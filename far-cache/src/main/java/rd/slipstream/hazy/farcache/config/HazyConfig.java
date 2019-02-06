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

package rd.slipstream.hazy.farcache.config;

import java.util.logging.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.EntryListenerConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.config.MultiMapConfig.ValueCollectionType;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.map.impl.MapListenerAdapter;

import rd.jsonmapper.JSON;

/**
 * @author indroneel
 *
 */

public class HazyConfig {

	private static Logger LOGGER = Logger.getLogger(HazyConfig.class.getName());

	public static final String MNAME_SESSIONS       = "hazy-sessions";
	public static final String MNAME_SESSION_KEYS   = "hazy-session-keys";
	public static final String MNAME_SESSION_VALUES = "hazy-session-values";

	@JSON("address")
	private String address;

	@JSON("port")
	private int port;

	@JSON("management-center")
	private MancentConfig mgmtctrCfg;

	@JSON("cluster")
	private ClusterConfig clusterCfg;

	@JSON("session")
	private SessionConfig sessionCfg;

	public HazyConfig() {
		sessionCfg = new SessionConfig();
	}

	public final Config createHazelcastConfig(MapListenerAdapter<String, Object> timeoutHandler) {
		Config cfg = new Config();
		cfg.setInstanceName("hazy");

		NetworkConfig netcfg = cfg.getNetworkConfig();
		if(address != null && address.trim().length() > 0) {
			netcfg.setPublicAddress(address);
		}
		netcfg.setPort(port >= 0 ? port : 0);
		netcfg.setPortAutoIncrement(false);
		netcfg.setPortCount(1);

		if(mgmtctrCfg != null) {
			mgmtctrCfg.populate(cfg);
		}

		if(clusterCfg != null) {
			clusterCfg.populate(cfg);
		}

		////

		int bacnt = sessionCfg.getBackupCount();
		LOGGER.info(String.format("hazy backup count %d", bacnt));
		int sessionTTL = sessionCfg.getTimeToLive();
		LOGGER.info(String.format("hazy-sessions TTL in seconds %d", sessionTTL));

		MapConfig hazySessions = new MapConfig(MNAME_SESSIONS);
		hazySessions.setBackupCount(bacnt);
		hazySessions.setTimeToLiveSeconds(sessionTTL);
		hazySessions.addEntryListenerConfig(
				new EntryListenerConfig(timeoutHandler, false, false));
		cfg.addMapConfig(hazySessions);

		MultiMapConfig hazySessionKeys = new MultiMapConfig(MNAME_SESSION_KEYS);
		hazySessionKeys.setBackupCount(bacnt);
		hazySessionKeys.setValueCollectionType(ValueCollectionType.SET);
		cfg.addMultiMapConfig(hazySessionKeys);

		MapConfig hazySessionValues = new MapConfig(MNAME_SESSION_VALUES);
		hazySessionValues.setBackupCount(bacnt);
		cfg.addMapConfig(hazySessionValues);

		return cfg;
	}
}
