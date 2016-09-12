package io.pkts.packet.sip.impl;

import java.io.IOException;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.TelURI;
import io.pkts.packet.sip.address.impl.TelURIImpl;

public class TelUriParser {

    private Buffer buffer;
    private Buffer original;

    public TelUriParser(Buffer buffer, Buffer original) {
        this.buffer = buffer;
        this.original = original;
    }
        
    public TelURI getTelUri() throws IndexOutOfBoundsException, IOException {
        Boolean globalNumber = null;
        Buffer phone = Buffers.EMPTY_BUFFER;
        Buffer params = Buffers.EMPTY_BUFFER;
        
        int idxInitPhone = -1;
        int idxEndPhone = -1;
        
        int idxInitParams = -1;
        int idxEndParams = -1;
        
        while(buffer.hasReadableBytes()) {
            byte b = buffer.readByte();
            if (globalNumber == null) {
                if (globalNumber = b == SipParser.PLUS) {
                    continue; // consume one more in case of a global number
                }
                
            }
            if (idxInitPhone == -1) {
                idxInitPhone = buffer.getReaderIndex() - 1; 
            }
            else if (idxEndPhone != -1 && idxInitParams == -1) {
                idxInitParams = buffer.getReaderIndex() - 1; 
            }
            
            if (idxEndPhone == -1) {
                if (b == SipParser.SEMI) { // end of phone
                    idxEndPhone = buffer.getReaderIndex() - 1; 
                }
                else if (isCRLFSP(b)) {
                    idxEndPhone = buffer.getReaderIndex() - 1; 
                    break;
                }
            }

            if (idxEndParams == -1 && isCRLFSP(b)) {
                    idxEndParams = buffer.getReaderIndex() - 1; 
                    break;
                }
        }
        
        int lastIndex = buffer.getReaderIndex();
        
        if (idxInitPhone == -1 || idxInitPhone == idxEndPhone) {
            throw new SipParseException(4,  "No phone number in tel URI");
        }
        else if (idxEndPhone == -1) { 
            // buffer ended so we take the last buffer position
            idxEndPhone = lastIndex;
        }
        buffer.setReaderIndex(idxInitPhone);
        phone = buffer.readBytes(idxEndPhone - idxInitPhone);
      
        if (idxInitParams != -1) {
            if (idxEndParams == -1) { 
                // buffer ended so we take the current position
                idxEndParams = lastIndex;
            }
            buffer.setReaderIndex(idxInitParams);
            params = buffer.readBytes(idxEndParams - idxInitParams);
        }
        
        while (buffer.hasReadableBytes() && isCRLFSP(buffer.peekByte())) {
            buffer.readByte(); 
        }
        
        return new TelURIImpl(globalNumber, phone, params, original);
    }


    private static boolean isCRLFSP(byte b) {
        return b == SipParser.LF || b == SipParser.CR || b == SipParser.SP;
    }

}
