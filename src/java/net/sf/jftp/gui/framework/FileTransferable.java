package net.sf.jftp.gui.framework;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class FileTransferable implements Transferable //, ClipboardOwner
{
    public static final DataFlavor plainTextFlavor = DataFlavor.plainTextFlavor;
    public static final DataFlavor[] flavors =
            {
                    FileTransferable.plainTextFlavor,
            };
    private static final List flavorList = Arrays.asList(flavors);

    public synchronized DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return (flavorList.contains(flavor));
    }

    public synchronized Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException,
            IOException {
        if (flavor.equals(FileTransferable.plainTextFlavor)) {
            return new ByteArrayInputStream(this.toString().getBytes("iso8859-1"));
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
