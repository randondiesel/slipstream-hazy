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

package rd.slipstream.hazy.farcache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.hazelcast.config.Config;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.hazelcast.map.impl.MapListenerAdapter;

import rd.jsonmapper.decode.Json2Object;
import rd.slipstream.hazy.farcache.config.HazyConfig;

/**
 * @author indroneel
 *
 */

public class Main {

	private static Logger LOGGER = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) throws Exception {
		//setup slf4j as the underlying logging mechanism.
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		if(args.length == 0) {
			LOGGER.severe("missing argument: configuration file name");
			return;
		}

		Main main = new Main();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				main.stop();
			}
		};
		Runtime.getRuntime().addShutdownHook(new Thread(runnable));

		main.start(args[0]);
		Thread.currentThread().join();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	private HazelcastInstance hinst;

	private void start(String cfgFileName) {
		if(LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine(String.format("reading configuration from: %s", cfgFileName));
		}

		byte[] cfgFileData;

		try {
			FileInputStream fis = new FileInputStream(new File(cfgFileName));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int oneByte;
			while((oneByte = fis.read()) >= 0) {
				bos.write(oneByte);
				byte[] buffer = new byte[fis.available()];
				int amt = fis.read(buffer);
				bos.write(buffer, 0, amt);
			}
			fis.close();
			bos.flush();
			cfgFileData = bos.toByteArray();
			bos.close();
		}
		catch(IOException exep) {
			if(LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, String.format("error reading configuration from: %s", cfgFileName), exep);
			}
			throw new RuntimeException(exep);
		}

		if(LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("setting up hazelcast server");
		}

		HazyConfig hazyCfg = (HazyConfig) new Json2Object(cfgFileData).convert(HazyConfig.class);

		Config cfg = hazyCfg.createHazelcastConfig(new SessionTimeoutHandler());
		hinst = Hazelcast.newHazelcastInstance(cfg);
	}

	private void stop() {
		if(hinst != null) {
			hinst.shutdown();
		}
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
			MultiMap<Object, String> sessionKeys = hinst.getMultiMap(HazyConfig.MNAME_SESSION_KEYS);
			Collection<String> keyNames = sessionKeys.remove(sessionId);
			IMap<String, Object> sessionValues = hinst.getMap(HazyConfig.MNAME_SESSION_VALUES);
			for(String key : keyNames) {
				sessionValues.remove(sessionId + ":" + key);
			}
		}
	}
}
