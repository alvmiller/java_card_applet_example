package extendedapplet1;

import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;

import javacard.framework.*;
import javacardx.framework.JCSystem;

public class ExtendedApplet1 extends Applet {

    private final static byte INS_GET_RESPONSE = (byte)0xC0;
    private final static byte INS_GET_DATA_61  = (byte)0xF2;
    private static final byte[] data_61 = {
        (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, 
        (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
        (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, 
        (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
        
        (byte) 0xBB, (byte) 0xBB, (byte) 0xBB, (byte) 0xBB, 
        (byte) 0xBB, (byte) 0xBB, (byte) 0xBB, (byte) 0xBB,
        (byte) 0xBB, (byte) 0xBB, (byte) 0xBB, (byte) 0xBB, 
        (byte) 0xBB, (byte) 0xBB, (byte) 0xBB, (byte) 0xBB
    };
    private final static short all_bytes_61 = (short)32;
    private static short last_bytes_61 = all_bytes_61;
    private static short start_index_61 = (short)0;

    private final static byte INS_GET_DATA_6C = (byte)0xF4;
    private static final byte[] data_6c = { (byte)0x01, (byte)0x02, (byte)0x03 };
    
    static final byte INS_PROCESS_DATA = (byte)0xF1;
    final byte[] process_data = { (byte)0xAA, (byte)0xBB, (byte)0xCC };
    
    static final byte INS_LONG_PROCESSING = (byte)0xF3;
    
    /**
     * Installs this applet.
     * 
     * @param bArray
     *            the array containing installation parameters
     * @param bOffset
     *            the starting offset in bArray
     * @param bLength
     *            the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new ExtendedApplet1();
    }

    /**
     * Only this class's install method should create the applet object.
     */
    protected ExtendedApplet1() {
        register();
    }

    /**
     * Processes an incoming APDU.
     * 
     * @see APDU
     * @param apdu
     *            the incoming APDU
     */
    @Override
    public void process(APDU apdu) {
        //Insert your code here
        byte[] apduBuffer = apdu.getBuffer();
        byte ins          = apduBuffer[ISO7816.OFFSET_INS];
        byte cla          = apduBuffer[ISO7816.OFFSET_CLA];
        byte p1           = apduBuffer[ISO7816.OFFSET_P1];
        byte p2           = apduBuffer[ISO7816.OFFSET_P2];

        switch (ins) {
            case INS_GET_DATA_61:
                cla &= (byte)0xFC;
                if (cla != (byte)0x80) {
                    ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
                }
                last_bytes_61 = all_bytes_61;
                start_index_61 = (short)0;
                get_data_61(apdu);
                break;
            case INS_GET_RESPONSE:
                if ((p1 != (byte)0x00) || (p2 != (byte)0x00)) {
                    ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
                }
                get_data_61(apdu);
                break;
            case INS_GET_DATA_6C:
                get_data_6c(apdu);
                break;
            case INS_PROCESS_DATA:
               process_test_data(apdu);
               break;
            case INS_LONG_PROCESSING:
                try {
                    some_waiting();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                process_test_data(apdu);
                break;
            default: {
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
            }
        }
        //Insert your code here
    }
    
    private void get_data_61(APDU apdu) {
        // Get ready to send
        short send_bytes = (short)0x00;

        short le = apdu.setOutgoing();
        if (le == (short)0x00) {
            le = (short)256;
        }

        if (le <= last_bytes_61) {
            send_bytes = le;
        } else {
            send_bytes = last_bytes_61;
        }

        apdu.setOutgoingLength((short)send_bytes);
        apdu.sendBytesLong(data_61, start_index_61, send_bytes);

        last_bytes_61  -= send_bytes;
        start_index_61 += send_bytes;

        if (last_bytes_61 != 0) {
            ISOException.throwIt((short)(ISO7816.SW_BYTES_REMAINING_00 | last_bytes_61));
        }
    }

    private void some_waiting() throws InterruptedException {
        // Get ready to send
        sleep(5000);
    }
    
    private void get_data_6c(APDU apdu) {
        // Get ready to send
        short le = apdu.setOutgoing();
        if (le < (short)data_6c.length) {
            ISOException.throwIt((short)(ISO7816.SW_CORRECT_LENGTH_00 | (short)data_6c.length));
        }

        apdu.setOutgoingLength((short)data_6c.length);
        apdu.sendBytesLong(data_6c, (short)0, (short)data_6c.length);
    }
    
    private void process_test_data(APDU apdu) {
        apdu.setOutgoing();
        apdu.setOutgoingLength((short)process_data.length);
        apdu.sendBytesLong(process_data, (short)0, (short)process_data.length);
    }
}
