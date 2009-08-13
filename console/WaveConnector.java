/**
 * Copyright 2009, Acknack Ltd. All rights reserved.
 * Copyright 2009 Google Inc.
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
 
/**
 * This code in this file is based on code originally written by Google Inc.
 * The original code can be found at:
 * http://code.google.com/p/wave-protocol/source/checkout
 */
package org.waveprotocol.wave.examples.fedone.waveclient.console;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.waveprotocol.wave.examples.fedone.waveclient.common.ClientBackend;
import org.waveprotocol.wave.examples.fedone.waveclient.common.ClientUtils;
import org.waveprotocol.wave.examples.fedone.waveclient.common.ClientWaveView;
import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
import org.waveprotocol.wave.examples.fedone.waveclient.common.IndexEntry;
import org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener;
import org.waveprotocol.wave.model.document.operation.BufferedDocOp;
import org.waveprotocol.wave.model.document.operation.impl.AttributesImpl;
import org.waveprotocol.wave.model.document.operation.impl.BufferedDocOpImpl.DocOpBuilder;
import org.waveprotocol.wave.model.operation.wave.AddParticipant;
import org.waveprotocol.wave.model.operation.wave.RemoveParticipant;
import org.waveprotocol.wave.model.operation.wave.WaveletDocumentOperation;
import org.waveprotocol.wave.model.document.operation.AnnotationBoundaryMap;
import org.waveprotocol.wave.model.document.operation.Attributes;
import org.waveprotocol.wave.model.document.operation.DocInitializationCursor;
import org.waveprotocol.wave.model.document.operation.impl.InitializationCursorAdapter;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.wave.ParticipantId;
import org.waveprotocol.wave.model.wave.data.WaveletData;

import java.io.IOException;
import java.io.PrintStream;

import java.lang.IllegalAccessException;
import java.lang.IndexOutOfBoundsException;
import java.lang.reflect.InvocationTargetException;
import java.lang.NoSuchMethodException;
import java.lang.reflect.Method;
import java.lang.SecurityException;
import java.lang.UnsupportedOperationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/*
* This provides a simple way to connect to the Google Wave server. The code is heavily based on
* the sample client provided by google and parts of the code are still Licensed under the Apache
* License, Version 2.0. See the source code for more details on this. The basis of this class is to
* provide a number of higher level commands that obscure the underlaying complexity involved with 
* communicating with the wave server in an effort to allow developers to quickly and easily develop
* applications that communicate with the wave server.
*
* Any applications you build should contain a publicly accessible "refresh" method. This method when
* called should request the inbox, open wave and participants from the server and redraw/refresh/update
* them in the user interface. This data periodically changes when for example, someone else posts a blip
* in a wave.
*
* It should also be noted that because the Google wave server works asynchronously if you modify a value
* then immediately request that value again from the server it may not have had time to register. For 
* example requesting a new wave then immediately requesting the wavelist. This is where the "refresh"
* method is key as this will propogate the changes to the calling class.
*
* Ensure that the connect method has been called with the correct arguments prior to accessing the other
* methods otherwise you will encounter a lot of {@link ServerNotConnectedException}
*/
public class WaveConnector implements WaveletOperationListener {
	
	
	private ClientBackend backend = null;
	private ClientWaveView openWave;
	private static final String MAIN_DOCUMENT_ID = "main";
	private final String LINE = "line";
	private final String LINE_AUTHOR = "by";
	private final Object callingClass;
	private final Method refreshMethod;
	private Map<ClientWaveView, HashedVersion> lastSeenVersions = Maps.newHashMap();
	
	
	
	/**
	* Constructor requires object containing calling method. Once you have called this class you
	* will need to create a public method named "refresh". This will be called when the inbox/wavw
	* is updated
	*
	* For example WaveConnector conn = new WaveConnector(this);
	*
	* @param Object contaning calling method
	*/
	public WaveConnector(Object callingClass) throws NoSuchMethodException, SecurityException{
		//Get the refresh method in parent
		this.callingClass = callingClass;
		try{
			refreshMethod = callingClass.getClass().getMethod("refresh");
		}catch(NoSuchMethodException e) {
			throw new NoSuchMethodException("Class named 'refresh' in calling method was not found. " +
			"'refresh' must be declared as a method that refreshes all variables in the user interface");
		}catch(SecurityException e) {
			throw new SecurityException("Class named 'refresh' in calling method was not accessible. " +
			"'refresh' must be declared as a method that refreshes all variables in the user interface");
		}
	}


	/**
	*	Initiate a connection with the server, registering the client.
	*/
	public void connect(String userAtDomain, String server, String portString) {
		//Parse and check provided port
		int port;
		try {
			port = Integer.parseInt(portString);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("A valid port must be provided");
		}
		
		//Connect to backend		
		try {
			backend = new ClientBackend(userAtDomain, server, port);
		} catch (IOException e) {
			throw new ServerNotConnectedException("Failed to connect: " + e.getMessage());
		}
		
		backend.addWaveletOperationListener(this);
	}
	
	
	
	/**
	*	Create a new wave with a randomly generated id
	*/
	public void createWave(){
		if(isConnected()) {
			backend.createNewWave();
		}
		else {
			throw new ServerNotConnectedException("Not connected to server. Unable to perform server operations while not connected to server");
		}
	}
	
	
	
	/**
	*	Open a wave of a given id. The id is the index of the wave.
	*
	* @param int containing the index
	*/
	public void openWave(int entry) {
		if(isConnected()) {
			List<IndexEntry> index = ClientUtils.getIndexEntries(backend.getIndexWave());
			
			//Check index is not out of bounds
			if(entry >= index.size()) {
				throw new IndexOutOfBoundsException("Cannot select wave " + entry + " does not exist");	
			}
			else {
				openWave = backend.getWave(index.get(entry).getWaveId());
				if (ClientUtils.getConversationRoot(openWave) == null) {
					openWave.createWavelet(ClientUtils.getConversationRootId(openWave));
				}
			}
		}
		else {
			throw new ServerNotConnectedException("Not connected to server. Unable to perform server operations while not connected to server");
		}
	}
	
	
	
	/**
	* Close the currently wave so that no waves are open
	*/
	public void closeOpenWave() {
		openWave = null;
	}
	
	
	
	/**
	* Mark all waves as read
	*/
	public void readAllWaves() {
		for (IndexEntry indexEntry : ClientUtils.getIndexEntries(backend.getIndexWave())) {
			ClientWaveView wave = backend.getWave(indexEntry.getWaveId());
			if ((wave != null) && (ClientUtils.getConversationRoot(wave) != null)) {
				lastSeenVersions.put(wave, wave.getWaveletVersion(ClientUtils.getConversationRootId(wave)));
			}
		}
	}
	
	
	
	/**
	* Append a new message to a wave
	*
	*@param String containing the text to be dded
	*/
	public void appendToWave(String text){
		if (isConnected()) {
			if(isWaveOpen()){
				BufferedDocOp openDoc = ClientUtils.getConversationRoot(openWave).getDocuments().get(MAIN_DOCUMENT_ID);
				int docSize = (openDoc == null) ? 0 : ClientUtils.findDocumentSize(openDoc);
				DocOpBuilder docOp = new DocOpBuilder();
				
				if (docSize > 0) {
					docOp.retain(docSize);
				}
				
				docOp.elementStart(
					LINE, new AttributesImpl(
							ImmutableMap.of(LINE_AUTHOR, backend.getUserId().getAddress())));
				docOp.elementEnd();
				docOp.characters(text);
				
				backend.sendWaveletOperation(
					ClientUtils.getConversationRoot(openWave),
						new WaveletDocumentOperation(MAIN_DOCUMENT_ID, docOp.finish()));
			} else {
				throw new NoWaveOpenException("No waves are open. Cannot perform this operation while no wave is open");
			}
		} else {
			throw new ServerNotConnectedException("Not connected to server. Unable to perform server operations while not connected to server");
		}
	}
	
	
	
	/**
	*	Return the inbox. Returns the inbox in an ArrayList
	*
	* @return An ArrayList of WaveId's contaning the id/domain representation of the waves the user is participating in
	*/
	public ArrayList<InboxElement> getInbox(){
		if(isConnected()) {
			updateLastSeenVersion();
			ClientWaveView indexWave = backend.getIndexWave();
		    List<IndexEntry> indexEntries = ClientUtils.getIndexEntries(indexWave);
		    ArrayList<InboxElement> inboxList = new ArrayList<InboxElement>();
		    
		    //Dump each result in the inbox list
		    for(int i = 0; i < indexEntries.size(); i++) {
		    	ClientWaveView wave = backend.getWave(indexEntries.get(i).getWaveId());
		    	if(!wave.getWaveletVersion(ClientUtils.getConversationRootId(wave)).equals(lastSeenVersions.get(wave))) {
		    		inboxList.add(new InboxElement(wave.getWaveId().getId(), indexEntries.get(i).getDigest(), false));
		    	} else {
		    		inboxList.add(new InboxElement(wave.getWaveId().getId(), indexEntries.get(i).getDigest(), true));
		    	}
		    }
		    return inboxList;
			}
		else {
			throw new ServerNotConnectedException("Not connected to server. Unable to perform server operations while not connected to server");
		}
	}
	
	
	
	/**
	* Return the participants of the currently open wave
	*
	* @return An array of Strings containing the address' of participants partaking in the open wave
	*/
	public String[] getWaveParticipants(){
		if(isConnected()) {
			if(isWaveOpen()){
				List<ParticipantId> participantList = ClientUtils.getConversationRoot(openWave).getParticipants();
				int participantListLength = participantList.size();
				String[] participantsAddress = new String[participantListLength];
				
				for(int i = 0; i < participantListLength; i++) {
					participantsAddress[i] = participantList.get(i).getAddress();
				}
				
				return participantsAddress;
			} else {
				//throw new NoWaveOpenException("No waves are open. Cannot perform this operation while no wave is open");
				return new String[0];
			}
		}
		else {
			throw new ServerNotConnectedException("Not connected to server. Unable to perform server operations while not connected to server");
		}
	}
	
	
	
	
	/**
	* Return the wave as an ArrayList. Each Wavelet is of type {@link CWavelet}
	*
	* This method works in a rather complex way...
	* We need to store the message author + contents in a final variable as the data is within an inner class
	* To do this we have to have a wavelet container (CWaveletContainer) that stores the data as it is added.
	* Once all the data has been added the wavelet container places this in a wavelet {@link CWavelet}.
	* This wavelet can be returned through the container and placed in an array
	* Continue doing this until you have all the wavelets and return the wavelet array to the calling method 
	*
	* @return ArrayList of {@link CWavelet} containing the author and text of each wavelet.
	*/
	public ArrayList<CWavelet> getWaveBody(){
		if(isConnected()) {
			if(isWaveOpen()) {
				final ArrayList<CWavelet> waveBody = new ArrayList<CWavelet>();
				final CWaveletContainer cWaveletContainer = new CWaveletContainer();
				
				for (BufferedDocOp document : ClientUtils.getConversationRoot(openWave).getDocuments().values()) {
						
					document.apply(new InitializationCursorAdapter(
					new DocInitializationCursor() {
					@Override public void characters(String s) {
						cWaveletContainer.setText(s);//Message content
						waveBody.add(cWaveletContainer.getWavelet());//Add to array
					}
					
					@Override
					public void elementStart(String type, Attributes attrs) {
						if(type.equals(LINE)) {
							if(!attrs.containsKey(LINE_AUTHOR)) {
								throw new UnsupportedOperationException("A line must have an author. No author was supplied");
							}
							cWaveletContainer.setAuthor(attrs.get(LINE_AUTHOR));//Message Author
						}
						else {
							throw new UnsupportedOperationException("Unsupported element type. Only lines are supported at present");
						}          
					}
					
					@Override
					public void elementEnd() {}
					
					@Override public void annotationBoundary(AnnotationBoundaryMap map) {}
					}));
				}
				updateLastSeenVersion();
				return waveBody;
			} else {
				return new ArrayList<CWavelet>();
			}
		}
		else {
			throw new ServerNotConnectedException("Not connected to server. Unable to perform server operations while not connected to server");
		}
	}
	
	
	
	/**
	* Remove a participant from the open wave
	*
	* @params String contaning the participants name
	* @throws ParticipantManagementException when the participant could not be found in the open wave
	*/
	public void removeParticipant(String name) throws ParticipantManagementException{
		if(isConnected()) {
			if(isWaveOpen()) {
				ParticipantId removeId = new ParticipantId(name);
				WaveletData openWavelet = ClientUtils.getConversationRoot(openWave);
				if (openWavelet.getParticipants().contains(removeId)) {
					backend.sendWaveletOperation(openWavelet, new RemoveParticipant(removeId));
				} else {
					throw new ParticipantManagementException("The participant could not be found in the open wave.");
				}
			} else {
				throw new NoWaveOpenException("No waves are open. Cannot perform this operation while no wave is open");
			}
		} else {
			throw new ServerNotConnectedException("Not connected to server. Unable to perform server operations while not connected to server");
		}
	}
	
	
	
	/**
	* Add a participant to the open wave
	*
	* @params String contaning the participants name
	* @throws ParticipantManagementException when the participant is already participating in the wave
	*/
	public void addParticipant(String name) throws ParticipantManagementException{
		if(isConnected()) {
			if(isWaveOpen()) {
				ParticipantId addId = new ParticipantId(name);
				WaveletData openWavelet = ClientUtils.getConversationRoot(openWave);
				if(!openWavelet.getParticipants().contains(addId)) {
					backend.sendWaveletOperation(openWavelet, new AddParticipant(addId));
				} else {
					throw new ParticipantManagementException("The participant could not be added. Participant is already participating in this wave");
				}
			}
			else {
				throw new NoWaveOpenException("No waves are open. Cannot perform this operation while no wave is open");
			}
		} else {
			throw new ServerNotConnectedException("Not connected to server. Unable to perform server operations while not connected to server");
		}
	}
	
	
	/**
	* Return the ID of the currently open wave
	* 
	* @return string containing the ID of the open wave
	*/
	public String getOpenWaveIdString() {
		if(isConnected()) {
			if(isWaveOpen()) {
				return openWave.getWaveId().getId();
			} else {
				throw new NoWaveOpenException("No waves are open. Cannot perform this operation while no wave is open");
			}
		} else {
			throw new ServerNotConnectedException("Not connected to server. Unable to perform server operations while not connected to server");
		}
	}
	
	
	
	/**
	* Return the ID of the currently open wave
	* 
	* @return {@link WaveId} containing the ID and domain of the open wave
	*/
	public WaveId getOpenWaveId() {
		if(isConnected()) {
			if(isWaveOpen()) {
				return openWave.getWaveId();
			} else {
				throw new NoWaveOpenException("No waves are open. Cannot perform this operation while no wave is open");
			}
		} else {
			throw new ServerNotConnectedException("Not connected to server. Unable to perform server operations while not connected to server");
		}
	}
	
	
	
	/**
	* Return the address of the user
	* 
	* @return String containing the address of the user. For example tom@localhost.com
	*/
	public String getUserId() {
		return backend.getUserId().getAddress();
	}
	
	
	
	/**
	* Removes the user from the open wave
	*/
	public void removeSelfFromWave() {
		if(isConnected()) {
			if(isWaveOpen()) {
				try {
					removeParticipant(getUserId());
				} catch(ParticipantManagementException e) {
					//This will never occur
				}
			} else {
				throw new NoWaveOpenException("No waves are open. Cannot perform this operation while no wave is open");
			}
		} else {
			throw new ServerNotConnectedException("Not connected to server. Unable to perform server operations while not connected to server");
		}
	}
	
	
	
	/**
	* Return boolean if a wave is currently open or not
	* 
	* @return true if there is a wave open, false if there is not
	*/
	public boolean isWaveOpen() {
		if(openWave == null) {
			return false;
		}
		else {
			return true;
		}
	}
	

	
	/**
	* Return boolen if client is currently connected to the server
	*
	* @return true is client is connected, false if not
	*/
	public boolean isConnected(){
		if(backend == null) {
			return false;
		}
		else {
			return true;
		}
	}
	
	
	
	/**
	* Shuts down the connection between the client and server gracefully
	*/
	public void shutdown() {
		backend.shutdown();
	}
	
	
	
	/**
	* Overrides parent method. Unused.
	*/
	@Override
	public void waveletDocumentUpdated(WaveletData wavelet, WaveletDocumentOperation docOp) {
	}
	
	/**
	* Overrides parent method. Unused.
	*/
	@Override
	public void participantAdded(WaveletData wavelet, ParticipantId participantId) {
	}
	
	/**
	* Overrides parent method. Called when user is removed from a wave
	*/
	@Override
	public void participantRemoved(WaveletData wavelet, ParticipantId participantId) {
		if (isWaveOpen() && participantId.equals(backend.getUserId())) {
			// We might have been removed from our open wave (an impressively verbose check...)
			if (wavelet.getWaveletName().waveId.equals(openWave.getWaveId())) {
				openWave = null;
			}
		}
	}
	
	/**
	* Overrides parent method. Unused.
	*/
	@Override
	public void noOp(WaveletData wavelet) {
	}
	
	/**
	* Overrides parent method. Unused.
	*/
	@Override
	public void onDeltaSequenceStart(WaveletData wavelet) {
	}
	
	/**
	* Overrides parent method. Called when inbox or wave updates
	*/
	@Override
	public void onDeltaSequenceEnd(WaveletData wavelet){
		try{
			refreshMethod.invoke(callingClass);
		}catch(IllegalAccessException e) {
			
		}catch(InvocationTargetException e) {
			
		}
	}
	
	/*
	* Updates the last time the wave was seen
	*/
	private void updateLastSeenVersion() {
		if(openWave != null) {
				lastSeenVersions.put(openWave, openWave.getWaveletVersion(ClientUtils.getConversationRootId(openWave)));
			}
	}
}
