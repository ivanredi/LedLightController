

import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

import artnet4j.ArtNet;
import artnet4j.ArtNetException;
import artnet4j.ArtNetNode;
import artnet4j.ArtNetServer;
import artnet4j.events.ArtNetDiscoveryListener;
import artnet4j.events.ArtNetServerListener;
import artnet4j.packets.ArtDmxPacket;
import artnet4j.packets.ArtNetPacket;
import artnet4j.packets.ArtPollPacket;
import artnet4j.packets.ArtPollReplyPacket;
import artnet4j.packets.PacketType;

public class ArtNetConnector implements DmxUniversesConnector, ArtNetDiscoveryListener, ArtNetServerListener
{
	private ArrayList<ArtNetNode> nodes = new ArrayList<ArtNetNode>();
	private static ArtNet artNet = new ArtNet();
	
	static
	{
		try {
			artNet.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArtNetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int sequenceId = 0;
	
	private boolean gotResponse;
	private static Semaphore allowNextSend = new Semaphore(1, true);
	private static Semaphore artNetAccess = new Semaphore(1, true);
	private String currentlyPolledAdress;

	
	public ArtNetConnector(String[] ipAdresses)
	{
		try {
			artNetAccess.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		artNet.addServerListener(this);
		for (int i = 0; i < ipAdresses.length; i++) {
			ArtPollPacket artPollPacket = new ArtPollPacket();
			try {
				allowNextSend.acquire();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			currentlyPolledAdress = ipAdresses[i];
			artNet.unicastPacket(artPollPacket, ipAdresses[i]);
		}
		try {
			allowNextSend.acquire();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		allowNextSend.release();
		artNet.removeServerListener(this);
		artNetAccess.release();
	}

	public void discoverNode(ArtPollReplyPacket reply) {
		boolean newNode = reply.getIPAddress().toString().endsWith(currentlyPolledAdress);
		if (newNode) {
			ArtNetNode node  = reply.getNodeStyle().createNode();
			node.extractConfig(reply);
			discoveredNewNode(node);
		}
	}
	
	@Override
	public synchronized void render(int universeIndex, int[] colors)
	{
		System.out.println(universeIndex);
		if (nodes.size() <= universeIndex / 4) {
//			System.out.println(universeIndex);
			return;
		}
		ArtDmxPacket artDmxPacket = new ArtDmxPacket();
		ArtNetNode artNetNode = nodes.get(universeIndex / 4);
		artDmxPacket.setUniverse(artNetNode.getSubNet(), artNetNode.getDmxOuts()[universeIndex % 4]);
		byte[] buffer = new byte[colors.length * 3];
//		System.out.print("[");
		for (int i = 0; i < colors.length; i++) {
			buffer[i * 3] = (byte) (colors[i] % (256 * 256 * 256) / (256 * 256));
			buffer[i * 3 + 1] = (byte) (colors[i] % (256 * 256) / 256);
			buffer[i * 3 + 2] = (byte) (colors[i] % 256);
//			System.out.print(buffer[i * 3] + " " + colors[i]);
//			System.out.print(";");
		}
//		System.out.println("]");
		artDmxPacket.setDMX(buffer, buffer.length);
		artDmxPacket.setSequenceID(sequenceId++);
		sequenceId &= 255;
		
		artNet.unicastPacket(artDmxPacket, artNetNode.getIPAddress());
	}

	@Override
	public void artNetPacketBroadcasted(ArtNetPacket arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void artNetPacketReceived(ArtNetPacket arg0)
	{
		if (arg0.getType() == PacketType.ART_POLL_REPLY) {
			discoverNode((ArtPollReplyPacket) arg0);
		}
	}

	@Override
	public void artNetPacketUnicasted(ArtNetPacket arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void artNetServerStarted(ArtNetServer arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void artNetServerStopped(ArtNetServer arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void discoveredNewNode(ArtNetNode arg0)
	{
		nodes.add(arg0);
		allowNextSend.release();
	}

	@Override
	public void discoveredNodeDisconnected(ArtNetNode arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void discoveryCompleted(List<ArtNetNode> arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void discoveryFailed(Throwable arg0)
	{
		// TODO Auto-generated method stub
		
	}
	
}