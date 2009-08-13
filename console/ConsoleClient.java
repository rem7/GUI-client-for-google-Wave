/**
 * Copyright 2009, Acknack Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package org.waveprotocol.wave.examples.fedone.waveclient.console;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.NoSuchMethodException;
import java.lang.SecurityException;
import java.lang.StringBuilder;

import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;


public class ConsoleClient extends JFrame implements Runnable{
	private WaveConnector waveConn;
    private JList inboxList;
    private JList waveList;
    private JLabel participantsText;
    private JLabel currentUserLab;
    
    public static void main(String[] args) {
        new ConsoleClient(args);
    }

    //Constructor
    public ConsoleClient(String[] args) {
    	try{
    		waveConn = new WaveConnector(this);
    		waveConn.connect(args[0], args[1], args[2]);
        	new Thread(this).start();
    	}catch(NoSuchMethodException e) {
    		System.out.println(e);
    		System.out.println("CRITICAL");
    		waveConn.shutdown();
    		System.exit(1);
    	}catch(SecurityException e){
    		System.out.println(e);
    		System.out.println("CRITICAL");
    		waveConn.shutdown();
    		System.exit(1);
    	}
    }

    //Main thread
    public void run() {
        //-----------------Launch window
        JPanel mainPa = new JPanel();
        mainPa.setLayout(new BorderLayout());
        this.setContentPane(mainPa);


        //-----------------Menu bar
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        //File menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem exitMI = new JMenuItem("Exit");
        fileMenu.add(exitMI);


        exitMI.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            	waveConn.shutdown();
                System.exit(1);
            }
        });




        //-----------------Panels
        //Top level panels
        JPanel inboxPa = new JPanel();
        inboxPa.setLayout(new BoxLayout(inboxPa, BoxLayout.Y_AXIS));
        mainPa.add(inboxPa, BorderLayout.WEST);

        JPanel wavePa = new JPanel();
        wavePa.setLayout(new BorderLayout());
        mainPa.add(wavePa, BorderLayout.EAST);

        //Wave panel
        JPanel actionPa = new JPanel();
        wavePa.add(actionPa, BorderLayout.NORTH);

        JPanel waveContentPa = new JPanel();
        waveContentPa.setLayout(new BorderLayout());
        wavePa.add(waveContentPa, BorderLayout.CENTER);

        JPanel waveInteractPa = new JPanel();
        wavePa.add(waveInteractPa, BorderLayout.SOUTH);


        //-----------------Inbox panel content
        currentUserLab = new JLabel();
        inboxPa.add(currentUserLab);
        
        inboxList = new JList();
        inboxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inboxList.setLayoutOrientation(JList.VERTICAL);
        inboxList.setVisibleRowCount(-1);

        JScrollPane inboxListScroll = new JScrollPane(inboxList);
        inboxListScroll.setPreferredSize(new Dimension(250, 80));

        inboxPa.add(inboxListScroll);
        
        JButton newWaveButt = new JButton("New Wave");
        inboxPa.add(newWaveButt);
        newWaveButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	waveConn.createWave();
            	refresh();
            }
        });

        JButton openWaveButt = new JButton("Open Wave");
        inboxPa.add(openWaveButt);
        openWaveButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	int selectedIndex = inboxList.getSelectedIndex();
                if(selectedIndex != -1) {
                	waveConn.openWave(selectedIndex);
                	refresh();
                }
            }
        });
        
        JButton closeWaveButt = new JButton("Close Wave");
        inboxPa.add(closeWaveButt);
        closeWaveButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               	waveConn.closeOpenWave();
               	refresh();
            }
        });
        
        JButton allReadButt = new JButton("Mark all waves read");
        inboxPa.add(allReadButt);
        allReadButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               	waveConn.readAllWaves();
               	refresh();
            }
        });



        //-----------------Action panel content
        JButton addParticipantButt = new JButton("Add participant");
        actionPa.add(addParticipantButt);
        addParticipantButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if(waveConn.isWaveOpen()) {
	            	try{
	            		waveConn.addParticipant(JOptionPane.showInputDialog(null, "Who do you want to add?"));
	            	}catch(ParticipantManagementException excep){System.out.println(excep);}
	            	refresh();
            	}
            }
        });

        JButton removeParticipantButt = new JButton("Remove Participant");
        actionPa.add(removeParticipantButt);
        removeParticipantButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if(waveConn.isWaveOpen()) {
	            	try{
	            		waveConn.removeParticipant(JOptionPane.showInputDialog(null, "Who do you want to remove?"));
	            	}catch(ParticipantManagementException excep){System.out.println(excep);}
	            	refresh();
            	}
            }
        });

		JButton removeSelfButt = new JButton("Remove self");
        actionPa.add(removeSelfButt);
        removeSelfButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if(waveConn.isWaveOpen()) {
            		waveConn.removeSelfFromWave();
                	refresh();
            	}
            }
        });

        JButton refreshButt = new JButton("Test butt");
        actionPa.add(refreshButt);
        refreshButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //refresh();
            }
        });




        //-----------------Wave content panel content
        //Participants
        JPanel participantsPa = new JPanel();
        participantsPa.add(new JLabel("Participants:"));
        participantsText = new JLabel("");
        participantsPa.add(participantsText);
        waveContentPa.add(participantsPa, BorderLayout.NORTH);



        //Main conversation
        waveList = new JList();
        waveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        waveList.setLayoutOrientation(JList.VERTICAL);
        waveList.setVisibleRowCount(-1);

        JScrollPane waveListScroll = new JScrollPane(waveList);
        waveListScroll.setPreferredSize(new Dimension(250, 80));

        waveContentPa.add(waveListScroll, BorderLayout.CENTER);


        //-----------------Wave interact panel content
        final JTextField newEntryText = new JTextField(20);
        waveInteractPa.add(newEntryText);
        JButton sendButt = new JButton("Send");
        waveInteractPa.add(sendButt);



        sendButt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if(waveConn.isWaveOpen()) {
	            	waveConn.appendToWave(newEntryText.getText());
	            	newEntryText.setText("");
	            	refresh();
            	}
            }
        });










        //---------------Finishing setting up window
        this.setTitle("GWave Client");
        this.setSize(1000, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
        


    }
    
    
    

	public void refresh() {
		updateInbox();
		updateWave();
	}

    private void updateInbox(){
    	currentUserLab.setText(waveConn.getUserId());
        ArrayList<InboxElement> inbox = waveConn.getInbox();

        DefaultListModel model = new DefaultListModel();
        StringBuilder tempStr;
        InboxElement tempElem;
        
        for(int i = 0; i < inbox.size(); i++) {
        	tempStr = new StringBuilder();
        	tempElem = inbox.get(i);
        	
        	//Construct the element
        	if(tempElem.getRead() == false) {
        		tempStr.append("[NEW] ");
        	}
        	tempStr.append("(" + tempElem.getId() + ") ");
        	tempStr.append(tempElem.getDigest());
        	
        	//Add to model
        	model.addElement(tempStr);
        }
        inboxList.setModel(model);
    }




    private void updateWave() {
    	//if(waveConn.isWaveOpen())
    	//{
	    	//Update participants
	    	
	    	String[] participants = waveConn.getWaveParticipants();
	    	if(participants != null) {
		    	StringBuilder participantsString = new StringBuilder();
				for(int i = 0; i < participants.length; i++) {
					participantsString.append(participants[i]);
					participantsString.append(", ");
				}
		        participantsText.setText(participantsString.toString());
	    	}
	        
	        
	        
	        //Update wave
	        ArrayList<CWavelet> waveBody = waveConn.getWaveBody();
	        if(waveBody != null) {
				CWavelet tempWavelet;
				DefaultListModel model = new DefaultListModel();
				
				for(int i = 0; i < waveBody.size(); i++) {
					tempWavelet = waveBody.get(i);
					model.addElement(tempWavelet.getAuthor() + " SAYS: " + tempWavelet.getText());
				}
				waveList.setModel(model);
	        }
    	//}
    }

}


