/*
 * Copyright (c) The original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package rd.slipstream.hazy.capability;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.hazelcast.config.Config;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.hazelcast.map.impl.MapListenerAdapter;

import rd.slipstream.hazy.capability.config.HazyConfig;

/**
 * @author indroneel
 *
 */

public class Peer2PeerProvider extends SessionManagerBase {

	private static Logger LOGGER = Logger.getLogger(Peer2PeerProvider.class.getName());

	private HazyConfig hazyCfg;

	public Peer2PeerProvider(HazyConfig cfg, ServletContext ctxt) {
		hazyCfg = cfg;
		super.ctxt = ctxt;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Methods of interface SessionDataManager

	@Override
	public void initialize() {
		Config cfg = hazyCfg.peer2PeerConfig().createHazelcastConfig(new SessionTimeoutHandler());
		super.hzi = Hazelcast.newHazelcastInstance(cfg);
		super.initialize();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Session timeout handler

	private class SessionTimeoutHandler extends MapListenerAdapter<String, Object> {

		@Override
		public void entryExpired(EntryEvent<String, Object> event) {
			if(LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine(String.format("session expired: %s", event.getKey()));
			}
			removeSession(event.getKey());
		}

		@Override
		public void entryEvicted(EntryEvent<String, Object> event) {
			if(LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine(String.format("session evicted: %s", event.getKey()));
			}
			removeSession(event.getKey());
		}

		@Override
		public void entryRemoved(EntryEvent<String, Object> event) {
			if(LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine(String.format("session removed: %s", event.getKey()));
			}
			removeSession(event.getKey());
		}

		private void removeSession(String sessionId) {
			MultiMap<Object, String> skMap = hzi.getMultiMap(MNAME_SESSION_KEYS);
			Collection<String> keyNames = skMap.remove(sessionId);
			IMap<String, Object> svMap = hzi.getMap(MNAME_SESSION_VALUES);
			for(String key : keyNames) {
				svMap.remove(sessionId + ":" + key);
			}
		}
	}
}
