package recplay;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JSplitPane;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JList;

import com.phybots.entity.MindstormsNXT;
import com.phybots.entity.MindstormsNXT.MindstormsNXTExtension;
import com.phybots.gui.ImageProviderPanel;
import com.phybots.service.Camera;
import com.phybots.task.NXTMotorControl;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import jp.digitalmuseum.connector.FantomConnector;

public class NXTRecordAndPlayFrame extends JFrame {
	private static final long serialVersionUID = -8521206425492264981L;
	private JPanel contentPane;
	private JList<Pose> list;
	private transient Camera camera;
	private transient NXTMotorControl nmc;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		// Start the camera.
		final Camera camera = new Camera();
		camera.start();

		// Find connected NXT.
		String[] ids = FantomConnector.queryIdentifiers();
		if (ids == null) {
			System.err.println("No NXT found.");
			return;
		}

		// Instantiate NXT.
		FantomConnector fc = new FantomConnector(ids[0]);
		fc.connect();
		final MindstormsNXT nxt = new MindstormsNXT(fc);
		nxt.removeDifferentialWheels();
		nxt.addExtension(
				new MindstormsNXTExtension(
						nxt, MindstormsNXT.Port.A));
//		nxt.addExtension(
//				new MindstormsNXTExtension(
//						nxt, MindstormsNXT.Port.B));
//		nxt.addExtension(
//				new MindstormsNXTExtension(
//						nxt, MindstormsNXT.Port.C));

		// Setup motors instance.
		final NXTMotorControl nmc = new NXTMotorControl(
			new MindstormsNXTExtension[] {
				nxt.requestResource(MindstormsNXTExtension.class, nxt),
				nxt.requestResource(MindstormsNXTExtension.class, nxt),
				nxt.requestResource(MindstormsNXTExtension.class, nxt)
			}, fc);
		nmc.start();

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				NXTRecordAndPlayFrame frame = new NXTRecordAndPlayFrame(camera, nmc);
				frame.setVisible(true);
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public NXTRecordAndPlayFrame(Camera camera_, NXTMotorControl nmc_) {
		this.camera = camera_;
		this.nmc = nmc_;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JSplitPane splitPane = new JSplitPane();
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		JPanel panel = new ImageProviderPanel(camera);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.setPreferredSize(new Dimension(640, 480));
		splitPane.setLeftComponent(panel);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(5, 5, 5, 5));
		splitPane.setRightComponent(panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JButton btnRecordPose = new JButton("Record");
		btnRecordPose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchronized (NXTRecordAndPlayFrame.this) {
					Pose pose = new Pose();
					pose.a = nmc.getRotationCount(MindstormsNXT.Port.A);
					pose.b = nmc.getRotationCount(MindstormsNXT.Port.B);
					pose.c = nmc.getRotationCount(MindstormsNXT.Port.C);
					((DefaultListModel<Pose>)list.getModel()).addElement(pose);
				}
			}
		});
		GridBagConstraints gbc_btnRecordPose = new GridBagConstraints();
		gbc_btnRecordPose.weightx = 1.0;
		gbc_btnRecordPose.fill = GridBagConstraints.BOTH;
		gbc_btnRecordPose.insets = new Insets(0, 0, 5, 5);
		gbc_btnRecordPose.gridx = 0;
		gbc_btnRecordPose.gridy = 0;
		panel_1.add(btnRecordPose, gbc_btnRecordPose);
		
		JButton btnPlayPose = new JButton("Play");
		btnPlayPose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchronized (NXTRecordAndPlayFrame.this) {
					Pose pose = list.getSelectedValue();
					if (pose != null) {
						nmc.setRotationCount(pose.a, MindstormsNXT.Port.A);
						nmc.setRotationCount(pose.b, MindstormsNXT.Port.B);
						nmc.setRotationCount(pose.c, MindstormsNXT.Port.C);
					}
				}
			}
		});
		GridBagConstraints gbc_btnPlayPose = new GridBagConstraints();
		gbc_btnPlayPose.weightx = 1.0;
		gbc_btnPlayPose.fill = GridBagConstraints.BOTH;
		gbc_btnPlayPose.insets = new Insets(0, 0, 5, 0);
		gbc_btnPlayPose.gridx = 1;
		gbc_btnPlayPose.gridy = 0;
		panel_1.add(btnPlayPose, gbc_btnPlayPose);
		
		list = new JList<Pose>(new DefaultListModel<Pose>());
		GridBagConstraints gbc_list = new GridBagConstraints();
		gbc_list.gridwidth = 2;
		gbc_list.insets = new Insets(0, 0, 0, 5);
		gbc_list.fill = GridBagConstraints.BOTH;
		gbc_list.gridx = 0;
		gbc_list.gridy = 1;
		panel_1.add(list, gbc_list);

		pack();
	}

}
