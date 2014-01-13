package com.phybots.picode.api.remote;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import com.phybots.Phybots;
import com.phybots.picode.api.MindstormsNXT;
import com.phybots.picode.api.MindstormsNXTPose;

public class MindstormsNXTServer {
	private static MindstormsNXTServer instance;
	private TServer server;
	private Map<Integer, MindstormsNXT> nxts;
	private int id = 0;
	private Future<?> future;

	public static MindstormsNXTServer getInstance() {
		if (instance == null) {
			instance = new MindstormsNXTServer();
		}
		return instance;
	}

	private MindstormsNXTServer() {
		nxts = new HashMap<Integer, MindstormsNXT>();
	}

	public void register(MindstormsNXT nxt) {
		if (!contains(nxt)) {
			nxts.put(id ++, nxt);
			//System.out.println("server: registered " + id);
		}
	}

	public void unregister(MindstormsNXT nxt) {
		for (Iterator<Entry<Integer, MindstormsNXT>> it = nxts.entrySet().iterator(); it.hasNext();) {
			if (it.next().getValue() == nxt) {
				it.remove();
			}
		}
	}

	public boolean contains(MindstormsNXT nxt) {
		return nxts.containsValue(nxt);
	}

	public boolean start() {
		if (future != null) {
			return true;
		}
		try {
			TServerTransport serverTransport = new TServerSocket(
					MindstormsNXTServiceConstants.SERVER_DEFAULT_PORT);
			TThreadPoolServer.Args args = new Args(serverTransport);
			args.processor(new MindstormsNXTService.Processor<MindstormsNXTService.Iface>(
					new MindstormsNXTServiceImpl()));
			server = new TThreadPoolServer(args);
			future = Phybots.getInstance().submit(new Runnable() {
				
				@Override
				public void run() {
					//System.out.println("server: start");
					server.serve();
					//System.out.println("server: stop");
				}
			});
			return true;
		} catch (TTransportException e) {
			return false;
		}
	}

	public void stop() {
		if (future != null) {
			server.stop();
			try {
				future.get(300, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				// Do nothing.
			}
			future.cancel(true);
		}
	}

	private class MindstormsNXTServiceImpl implements MindstormsNXTService.Iface {
		
		@Override
		public int connect(String identifier) throws TException {
			//System.out.println("server: look for " + identifier);
			if (identifier == null) return -1;
			for (Entry<Integer, MindstormsNXT> entry : nxts.entrySet()) {
				if (identifier.equals(entry.getValue().getIdentifier())) {
					//System.out.println("server: found");
					return entry.getValue().connect() ? entry.getKey() : -1;
				}
			}
			return -1;
		}
		
		@Override
		public MindstormsNXTPoseData getPose(int id) throws TException {
			MindstormsNXT nxt = nxts.get(id);
			MindstormsNXTPoseData data = new MindstormsNXTPoseData();
			if (nxt == null) {
				data.a = data.b = data.c = 0;
				return data;
			}
			MindstormsNXTPose pose = (MindstormsNXTPose) nxt.getPose();
			int[] rawData = pose.getData();
			data.a = rawData[0];
			data.b = rawData[0];
			data.c = rawData[0];
			return data;
		}
		
		@Override
		public boolean setPose(int id, MindstormsNXTPoseData data)
				throws TException {
			MindstormsNXT nxt = nxts.get(id);
			if (nxt == null) {
				return false;
			}
			MindstormsNXTPose pose = new MindstormsNXTPose();
			pose.importData(new int[] { data.a, data.b, data.c });
			return nxt.setPose(pose);
		}
		
		@Override
		public boolean isActing(int id) throws TException {
			MindstormsNXT nxt = nxts.get(id);
			return nxt != null && nxt.isActing();
		}
		
		@Override
		public void reset(int id) throws TException {
			MindstormsNXT nxt = nxts.get(id);
			if (nxt != null) {
				nxt.getMotorManager().reset();
			}
		}
	}

}
