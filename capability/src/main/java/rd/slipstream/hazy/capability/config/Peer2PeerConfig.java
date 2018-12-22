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

package rd.slipstream.hazy.capability.config;

import java.util.logging.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.EntryListenerConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.config.MultiMapConfig.ValueCollectionType;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.map.impl.MapListenerAdapter;

import rd.jsonmapper.JSON;

/**
 * @author indroneel
 *
 */

public class Peer2PeerConfig implements HazyConstants {

	private static Logger LOGGER = Logger.getLogger(Peer2PeerConfig.class.getName());

	@JSON("address")
	private String address;

	@JSON("port")
	private int port;

	@JSON("management-center")
	private MancentConfig mgmtctrCfg;

	@JSON("multicast-group")
	private String mcastGrp;

	@JSON("multicast-port")
	private int mcastPort;

	@JSON("multicast-timeout")
	private int mcastTimeout;

	@JSON("multicast-ttl")
	private int mcastTTL;

	@JSON("session-backup-count")
	private int sessionBackupCount;

	@JSON("session-ttl")
	private int sessionTtl;

	public Peer2PeerConfig() {
		mcastGrp = "224.225.226.227";
		mcastPort = 54327;
		mcastTimeout = 10;
		mcastTTL = 32;
		sessionBackupCount = 3;
		sessionTtl = 300;
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

		MulticastConfig mccfg = netcfg.getJoin().getMulticastConfig();
		mccfg.setEnabled(true);

		mccfg.setMulticastGroup(mcastGrp);
		mccfg.setMulticastPort(mcastPort);
		mccfg.setMulticastTimeoutSeconds(mcastTimeout);
		mccfg.setMulticastTimeToLive(mcastTTL);

		////

		LOGGER.info(String.format("hazy backup count %d", sessionBackupCount));
		LOGGER.info(String.format("hazy-sessions TTL in seconds %d", sessionTtl));

		MapConfig hazySessions = new MapConfig(MNAME_SESSIONS);
		hazySessions.setBackupCount(sessionBackupCount);
		hazySessions.setTimeToLiveSeconds(sessionTtl);
		hazySessions.addEntryListenerConfig(
				new EntryListenerConfig(timeoutHandler, false, false));
		cfg.addMapConfig(hazySessions);

		MultiMapConfig hazySessionKeys = new MultiMapConfig(MNAME_SESSION_KEYS);
		hazySessionKeys.setBackupCount(sessionBackupCount);
		hazySessionKeys.setValueCollectionType(ValueCollectionType.SET);
		cfg.addMultiMapConfig(hazySessionKeys);

		MapConfig hazySessionValues = new MapConfig(MNAME_SESSION_VALUES);
		hazySessionValues.setBackupCount(sessionBackupCount);
		cfg.addMapConfig(hazySessionValues);

		return cfg;
	}
}
