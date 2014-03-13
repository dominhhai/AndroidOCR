/* 
 * File:   TessLeptWapper.cpp
 * Author: dominhhai
 *
 * Created on March 27, 2013, 1:58 AM
 */

#include "TessLeptWapper.h"
#include "OCRHandler.h"
#include <iostream>
#include <string.h>
#include "strngs.h"
#include "baseapi.h"
#include "allheaders.h"
#include "imageio.h"

using namespace std;

JNIEXPORT jstring JNICALL Java_enginewapper_TessLeptWapper_processOCR
(JNIEnv *env, jobject obj,
        jbyteArray pImage, jstring pLanguage,
        jstring pDatapath, jint pOCREngineMode,
        jboolean isCallTesseractOnly) {
    // convert data
    jint size = env->GetArrayLength(pImage);
    jbyte *buffer = env->GetByteArrayElements(pImage, 0);
    unsigned char* inputdata = reinterpret_cast<unsigned char*> (buffer);
    const char* language = env->GetStringUTFChars(pLanguage, 0);
    const char* datapath = env->GetStringUTFChars(pDatapath, 0);

    // call engine
    OCRHandler ocrHandler(inputdata, size, language,
            pOCREngineMode, datapath, isCallTesseractOnly);

    // 0 : OCR fail, 1 : OCR success
    bool success = ocrHandler.ProcessOCR();
    STRING result_id = "0";
    if (success) {
        result_id = "1";
    }
    STRING result = result_id + ocrHandler.GetResult();
//
//    PIX* deskewdata = ocrHandler.GetDeskewedPix();
//    int wr = pixWrite("deskewdata.png", deskewdata, IFF_PNG);
//    if (wr == 0) {
//        cout << "write ok";
//    } else {
//        cout << "Write error!";
//    }

    // free memory


    return env->NewStringUTF(result.string());
}