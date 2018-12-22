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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.EntryView;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;

import rd.slipstream.ext.SessionData;

/**
 * @author indroneel
 *
 */

public class HazySessionData implements SessionData {

/**
 * This is the builder class that is used to create an instance of HazySessionData. The builder is
 * stateful, with each instance should be used to create exactly one session instancem, ever.
 */

	public static class Builder {

		private HazySessionData hzsd;

		public Builder() {
			hzsd = new HazySessionData();
		}

		public Builder sessionsMap(IMap<String, SessionMeta> smap) {
			hzsd.sessions = smap;
			return this;
		}

		public Builder sessionKeysMap(MultiMap<String, String> skmap) {
			hzsd.sessionKeys = skmap;
			return this;
		}

		public Builder sessionValuesMap(IMap<String, Object> svmap) {
			hzsd.sessionValues = svmap;
			return this;
		}

		public HazySessionData createNew() {
			UUID uuid = UUID.randomUUID();
			StringBuilder sb = new StringBuilder();
			sb.append(Long.toString(uuid.getMostSignificantBits(), 36))
				.append('-')
				.append(Long.toString(uuid.getMostSignificantBits(), 36));
			String sessionId = sb.toString().toLowerCase();

			if(hzsd.sessions.containsKey(sessionId)) {
				// if a session already exists with the given id, do not create a new one.
				return null;
			}

			SessionMeta smeta = new SessionMeta();
			smeta.setCreationTime(System.currentTimeMillis());
			smeta.setLastAccessTime(Long.MIN_VALUE);
			hzsd.sessions.put(sessionId, smeta);

			hzsd.sessionId = sessionId;
			hzsd.sessionMeta = smeta;
			return hzsd;
		}

		public HazySessionData getExisting(String sessionId) {

			if(!hzsd.sessions.containsKey(sessionId)) {
				// a session must exist already with the given id, else return null.
				return null;
			}

			SessionMeta smeta = hzsd.sessions.get(sessionId);
			smeta.setLastAccessTime(System.currentTimeMillis());
			hzsd.sessions.put(sessionId, smeta);

			hzsd.sessionId = sessionId;
			hzsd.sessionMeta = smeta;
			return hzsd;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	private IMap<String, SessionMeta> sessions;
	private MultiMap<String, String>  sessionKeys;
	private IMap<String, Object>      sessionValues;

	private String      sessionId;
	private SessionMeta sessionMeta;

	@Override
	public String getId() {
		return sessionId;
	}

	@Override
	public long getCreationTime() {
		return sessionMeta.getCreationTime();
	}

	@Override
	public long getLastAccessedTime() {
		return sessionMeta.getLastAccessTime();
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		SessionMeta meta = sessions.get(sessionId);
		sessions.put(sessionId, meta, interval, TimeUnit.SECONDS);
	}

	@Override
	public int getMaxInactiveInterval() {
		EntryView<String, SessionMeta> ev = sessions.getEntryView(sessionId);
		return (int) ev.getTtl();
	}

	@Override
	public Object getAttribute(String name) {
		return sessionValues.get(sessionId + ":" + name);
	}

	@Override
	public Set<String> getAttributeNames() {
		return new HashSet<>(sessionKeys.get(sessionId));
	}

	@Override
	public void setAttribute(String name, Object value) {
		sessionValues.put(sessionId + ":" + name, value);
		sessionKeys.put(sessionId, name);
	}

	@Override
	public void removeAttribute(String name) {
		sessionKeys.remove(sessionId, name);
		sessionValues.remove(sessionId + ":" + name);
	}

	@Override
	public void invalidate() {
		sessions.remove(sessionId);
	}
}
