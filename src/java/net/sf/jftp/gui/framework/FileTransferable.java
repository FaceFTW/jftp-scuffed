package net.sf.jftp.gui.framework;

import java.awt.datatransfer.*;
import java.awt.dnd.*;

import java.io.*;

import java.util.*;


public class FileTransferable implements Transferable //, ClipboardOwner
{
    public static final DataFlavor plainTextFlavor = DataFlavor.plainTextFlavor;
    public static final DataFlavor[] flavors = 
                                               {
                                                   FileTransferable.plainTextFlavor,
                                               };
    private static final List flavorList = Arrays.asList(flavors);

    public synchronized DataFlavor[] getTransferDataFlavors()
    {
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return (flavorList.contains(flavor));
    }

    public synchronized Object getTransferData(DataFlavor flavor)
                                        throws UnsupportedFlavorException, 
                                               IOException
    {
        if(flavor.equals(FileTransferable.plainTextFlavor))
        {
            return new ByteArrayInputStream(this.toString().getBytes("iso8859-1"));
        }
        else
        {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
