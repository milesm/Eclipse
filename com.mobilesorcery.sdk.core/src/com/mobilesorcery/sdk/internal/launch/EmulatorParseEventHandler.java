/**
 * 
 */
package com.mobilesorcery.sdk.internal.launch;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;

import com.mobilesorcery.sdk.core.CoreMoSyncPlugin;
import com.mobilesorcery.sdk.core.MoSyncProject;
import com.mobilesorcery.sdk.internal.EmulatorOutputParser;
import com.mobilesorcery.sdk.internal.SLD;
import com.mobilesorcery.sdk.internal.EmulatorOutputParser.ParseEvent;

public class EmulatorParseEventHandler implements EmulatorOutputParser.IParseEventHandler {

    /**
     * A listener interface for close listeners
     * @author Mattias
     *
     */
    /*interface ICloseListener {
        public void closed();
    }*/
    
    private MoSyncProject project = null;
    private SLD sld = null;

    private PipedOutputStream messageStream;

    private CountDownLatch sldLatch = null;

    private String exitMessage;
    
    private int emulatorId = -1;

    public EmulatorParseEventHandler(MoSyncProject project) {
        this.project = project;
        startSLDParsing();
    }

    private void startSLDParsing() {
    	sldLatch = new CountDownLatch(1);
    	Runnable sldRunnable = new Runnable() {
			public void run() {
				try {
					SLD oldSLD = sld;
					sld = project.parseSLD();
					if (CoreMoSyncPlugin.getDefault().isDebugging()) {
						if (oldSLD == sld) {
							CoreMoSyncPlugin.trace("Using cached SLD for " + project.getName());
						} else {
							CoreMoSyncPlugin.trace("Done parsing sld for " + project.getName());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					sldLatch.countDown();
				}
			}
    	};
    	
    	Thread sldThread = new Thread(sldRunnable);    	
    	sldThread.setName("Parsing SLD for project " + project.getName());
    	sldThread.start();
	}

	public void setEmulatorId(int id) {
        this.emulatorId = id;
    }
    
    public void handleEvent(ParseEvent event) {
        int[] stack = event.stack;

        try {
            if (CoreMoSyncPlugin.getDefault().isDebugging()) {
            	CoreMoSyncPlugin.trace(event);
            }

            switch (event.type) {
            case EmulatorOutputParser.REPORT_STRING:
            case EmulatorOutputParser.REPORT_EXIT_STRING:
                messageStream.write(emulatorId(event.message).getBytes());
                messageStream.write('\n');
                break;
            case EmulatorOutputParser.REPORT_IP:
                stack = new int[] { event.ip };
                // fall thru
            case EmulatorOutputParser.REPORT_CALL_STACK:
                SLD sld = getSLD();
                for (int i = 0; stack != null && i < stack.length; i++) {
                    String filename = sld == null ? null : sld.getFileName(stack[i]);
                    int line = sld == null ? -1 : sld.getLine(stack[i]);
                    String sldMsg = (filename == null ? "Unknown file" : filename) + (line > 0 ? (":" + line) : "");
                    // console.addMessage("0x" + Integer.toHexString(stack[i]) +
                    // ": " + sldMsg);
                    messageStream.write(emulatorId("IP:0x" + Integer.toHexString(stack[i]) + ": " + sldMsg).getBytes());
                    messageStream.write('\n');
                }
                break;
            case EmulatorOutputParser.REPORT_LOAD_PROGRAM:
            case EmulatorOutputParser.REPORT_RELOAD:
                sld = null;
                break;
            }
        } catch (Exception e) {
            // Ignore.
        	e.printStackTrace();
        }
    }

    private String emulatorId(String msg) {
        return emulatorId > 0 ? "[" + emulatorId + "] " + msg : msg;
    }

    private SLD getSLD() throws IOException {
    	try {
    		if (sldLatch.getCount() > 0) {
    			messageStream.write("Reading line number information - may take a few moments\n".getBytes());
    		}
    		
			sldLatch.await();
			return sld;
		} catch (InterruptedException e) {
			throw new IOException(e.getMessage());
		}        
    }

    /**
     * @param messageStream
     */
    public void setMessageOutputStream(PipedOutputStream messageStream) {
        this.messageStream = messageStream;
    }

    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
    }

}