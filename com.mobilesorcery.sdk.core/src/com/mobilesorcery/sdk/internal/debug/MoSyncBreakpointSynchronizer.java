package com.mobilesorcery.sdk.internal.debug;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * A class that tries to mitigate strange CDT breakpoint
 * behaviour, like for instance a completely ignorant view
 * of matching -break-insert and the result of this -break-insert.
 * Also, double breakpoints are generated upon each new launch
 * (where more events are generated from mdb)
 * @author Mattias Bybro
 *
 */
public class MoSyncBreakpointSynchronizer implements IBreakpointListener {

	public class MoSyncBreakpointComparator implements Comparator<IBreakpoint> {

		public int compare(IBreakpoint bp1, IBreakpoint bp2) {
			if (bp1 instanceof ICLineBreakpoint && bp2 instanceof ICLineBreakpoint) {
				ICLineBreakpoint cbp1 = (ICLineBreakpoint) bp1;
				ICLineBreakpoint cbp2 = (ICLineBreakpoint) bp2;
				
				// TODO: Conditions, etc!? We may very well want several bp's on one line!
				IMarker mbp1 = cbp1.getMarker();
				IMarker mbp2 = cbp1.getMarker();
				
				try {
					int l1 = cbp1.getLineNumber();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// Just for transient comparisons, so this is ok
			return new Integer(System.identityHashCode(bp1)).compareTo(System.identityHashCode(bp2));
		}

	}

	private MoSyncCDebugTarget target;

	public MoSyncBreakpointSynchronizer() {
	}
	
	public void install() {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}
	
	public void uninstall() {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
	}
	
	public void cleanup() {
		IBreakpointManager bp = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] bps = bp.getBreakpoints(CDIDebugModel.getPluginIdentifier());
		
		TreeSet uniqueBps = new TreeSet(new MoSyncBreakpointComparator());
		uniqueBps.addAll(Arrays.asList(bps));
	}

	public void breakpointAdded(IBreakpoint breakpoint) {
		if (isBreakpointApplicable(breakpoint)) {
			//cleanup();
		}
	}

	private boolean isBreakpointApplicable(IBreakpoint breakpoint) {
		return CDIDebugModel.getPluginIdentifier().equals(breakpoint.getModelIdentifier());
	}

	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
	}

	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
	}
	
}
