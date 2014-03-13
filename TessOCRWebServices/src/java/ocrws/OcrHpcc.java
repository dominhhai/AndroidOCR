/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ocrws;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import enginewapper.TessLeptWapper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import postprocessing.Processor;
import preprocessing.AbsImagePreprocessing;
import preprocessing.ImageHelper;
import preprocessing.ImagePreprocessing;

/**
 * REST Web Service
 *
 * @author dominhhai
 */
@Path("ocrservice")
public class OcrHpcc {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of OcrHpcc
     */
    public OcrHpcc() {
    }

    /**
     * Retrieves representation of an instance of api.Tesseract
     *
     * @return an instance of java.lang.String
     */
    @GET
    public String getJson() {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postJson(@FormDataParam("image") InputStream uploadedInputStream,
            @FormDataParam("image") FormDataContentDisposition fileDetail,
            @FormDataParam("size") int imagesize,
            @FormDataParam("width") int width, @FormDataParam("height") int height,
            @DefaultValue("eng") @FormDataParam("lang") String language,
            @DefaultValue("3") @FormDataParam("psm") int pagesegmode,
            @DefaultValue("true") @FormDataParam("process") boolean isPrePostProcess) {

        byte[] inputBytes = null;
        JSONObject jsonResult = new JSONObject();
        // convert data from client
        //inputstream into byte array
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = uploadedInputStream.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            inputBytes = buffer.toByteArray();
            buffer.close();

        } catch (Exception ex) {
            Logger.getLogger(OcrHpcc.class.getName()).log(Level.SEVERE, null, ex);
        }
        // decompress data
        if (inputBytes != null) {
//            System.out.println ("image size : " + imagesize);
            Inflater inflater = new Inflater();
            inflater.setInput(inputBytes, 0, inputBytes.length);
            byte[] decompressedBytes = new byte[imagesize];
            try {
                if (inflater.inflate(decompressedBytes) != imagesize) {
                    System.out.println("size error : " + imagesize);
                    inputBytes = null;
                }
            } catch (DataFormatException ex) {
                inputBytes = null;
                Logger.getLogger(OcrHpcc.class.getName()).log(Level.SEVERE, null, ex);
            }
            inflater.end();

            if (inputBytes != null) {
                inputBytes = decompressedBytes;
            }
        }
        // data error handling
        if (inputBytes == null) {
            try {
                jsonResult.put(MessageTypes.HEADER, MessageTypes.ER_DATA_ERROR);
            } catch (JSONException ex) {
                Logger.getLogger(OcrHpcc.class.getName()).log(Level.SEVERE, null, ex);
            }
            return jsonResult.toString();
        }
        // -- end handling data error
        // -- end convert data from client
        // pre-processing
        if (isPrePostProcess) {
            AbsImagePreprocessing imgprocess = new ImagePreprocessing();
            imgprocess.setDebug(true);
            imgprocess.setFileName(fileDetail.getFileName());
            int[] inputInts = ImageHelper.bytetoint(inputBytes);
            imgprocess.setInputImage(inputInts, width, height);
            imgprocess.setDebug(false);
            imgprocess.process();
            inputBytes = imgprocess.getOutputImage();
        }
        if (inputBytes == null) {
            try {
                jsonResult.put(MessageTypes.HEADER, MessageTypes.ER_PRE_PROCESSING_ERROR);
            } catch (JSONException ex) {
                Logger.getLogger(OcrHpcc.class.getName()).log(Level.SEVERE, null, ex);
            }
            return jsonResult.toString();
        }
        // -- end pre-processing
        // call tesseract engine
        String result = TessLeptWapper.getInstance().processOCR(inputBytes, language, TessLeptWapper.DATA_PATH, pagesegmode, !isPrePostProcess);
//        String result = "1ok";
        // -- end calling tesseract engine
        // put header in json format
        char prefix = '0';
        if (result.length() > 0) {
            prefix = result.charAt(0);
        }
        if (result.length() > 1) {
            result = result.substring(1);
        } else {
            result = "";
        }
        try {
            jsonResult.put(MessageTypes.HEADER, (prefix == '0') ? MessageTypes.ER_OCR_ERROR : MessageTypes.OCR_OK);
        } catch (JSONException ex) {
            Logger.getLogger(OcrHpcc.class.getName()).log(Level.SEVERE, null, ex);
        }
        // -- end putting header
        // post-processing
        if (isPrePostProcess && (result.length() > 1)) {
            // trim this result
            result = result.trim();
            int i = 0;
            while (i < result.length() && this.isRemoveable(result.charAt(i))) {
                i++; // delete all space, newline, tab in the head of string
            }
            if (i > 0) {
                result = result.substring(i - 1);
            }
            // call post-processing
            try {
                // TODO call post-processing
                if (result.length() > 1) {
                    result = Processor.postProcess(result, language, "dangAmbigsPath", false);
                }
            } catch (Exception ex) {
                Logger.getLogger(OcrHpcc.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // -- end post-processing
        // add to jsonResult
        try {
            // TODO put result in json format
            jsonResult.put(MessageTypes.DATA, result);
        } catch (JSONException ex) {
            Logger.getLogger(OcrHpcc.class.getName()).log(Level.SEVERE, null, ex);
        }
        // -- end putting result
        // sent result to client
        return jsonResult.toString();
    }

    private boolean isRemoveable(char c) {
        return ((c == ' ') || (c == '\n') || (c == '\t'));
    }
}
