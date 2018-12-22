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

import javax.servlet.ServletContext;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;

import rd.slipstream.ext.SessionData;
import rd.slipstream.ext.SessionDataManager;
import rd.slipstream.hazy.capability.config.HazyConstants;

/**
 * @author indroneel
 *
 */

public abstract class SessionManagerBase implements SessionDataManager, HazyConstants {

	protected HazelcastInstance hzi;
	protected ServletContext    ctxt;

	private IMap<String, SessionMeta> sessions;
	private MultiMap<String, String>  sessionKeys;
	private IMap<String, Object>      sessionValues;

	protected SessionManagerBase() {
		//NOOP
	}

	@Override
	public void initialize() {
		sessions = hzi.getMap(MNAME_SESSIONS);
		sessionKeys = hzi.getMultiMap(MNAME_SESSION_KEYS);
		sessionValues = hzi.getMap(MNAME_SESSION_VALUES);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// Methods of interface SessionDataManager

	@Override
	public SessionData getSessionData() {
		return new HazySessionData.Builder()
			.sessionsMap(sessions)
			.sessionKeysMap(sessionKeys)
			.sessionValuesMap(sessionValues)
			.createNew();
	}

	@Override
	public SessionData getSessionData(String id) {
		return new HazySessionData.Builder()
			.sessionsMap(sessions)
			.sessionKeysMap(sessionKeys)
			.sessionValuesMap(sessionValues)
			.getExisting(id);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}
}