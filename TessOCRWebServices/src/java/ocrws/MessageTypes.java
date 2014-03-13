/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ocrws;

/**
 *
 * @author minhhai
 */
public final class MessageTypes {
    // header
    public static final String HEADER = "ocr";
    // data
    public static final String DATA = "result";
    // data error when extracting
    public static final byte ER_DATA_ERROR = 0;
    // data process error
    public static final byte ER_PRE_PROCESSING_ERROR = 1;
    // ocr error
    public static final byte ER_OCR_ERROR = 2;
    // ocr success
    public static final byte OCR_OK = 3;
    
    
}
