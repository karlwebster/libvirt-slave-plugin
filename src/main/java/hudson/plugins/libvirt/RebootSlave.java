package hudson.plugins.libvirt;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.slaves.ComputerLauncher;

import java.io.IOException;
import java.util.Map;
import java.util.logging.*;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;

// Supertype doesn't declare generic types, can't do anything to fix that and still be able to override.
@SuppressWarnings("rawtypes")
@Extension
public class RebootSlave extends RunListener<Run> {

    @Override
    public void onFinalized(Run r) {
        Node node = r.getExecutor().getOwner().getNode();

        Logger local_logger = Logger.getLogger("Reboot-Logger");

        if (node instanceof VirtualMachineSlave){
            VirtualMachineSlave slave = (VirtualMachineSlave) node;

            ComputerLauncher launcher = slave.getLauncher();

            if (launcher instanceof ComputerLauncher){
                VirtualMachineLauncher slaveLauncher = (VirtualMachineLauncher) launcher;

                if(slaveLauncher.getForceSlaveRebootGlobal())
                {
                    local_logger.info("Reboot-Logger: Force reboot set.");
                    String vmName = slaveLauncher.getVirtualMachineName();
                    local_logger.info("Reboot-Logger: Preparing to reboot " + vmName + ".");

                    try{
                        Computer computer = slave.getComputer();
                        try {
                            computer.getChannel().syncLocalIO();
                            try{
                                computer.getChannel().close();
                                computer.disconnect(null);
                                try{
                                    computer.waitUntilOffline();

                                    local_logger.info("Reboot-Logger: VM: " + vmName + " has been shut down.");

                                }
                                catch (InterruptedException e){
                                    local_logger.info("Reboot-Logger: Interrupted while waiting for computer to be offline: " + e);
                                }
                            }
                            catch (IOException e){
                                local_logger.info("Reboot-Logger: Error closing channel: " + e);
                            }
                        }
                        catch (InterruptedException e){
                            local_logger.info("Reboot-Logger: Interrupted while syncing IO: " + e);
                        }
                    }
                    catch(Exception e){
                        local_logger.info("Reboot-Logger: Error whilst getting Computer: " + e);
                    }
                }
                else{
                    local_logger.info("Reboot-Logger: Not rebooting VM due to Global or Job Force Reboot options not being set.");
                }
            }
        }
    }
}