/* 
 * File:   OCRHandler.h
 * Author: dominhhai
 *
 * Created on March 27, 2013, 1:58 AM
 */

#ifndef OCRHANDLER_H
#define	OCRHANDLER_H

#include "allheaders.h"
#include "baseapi.h"
#include "strngs.h"

class OCRHandler {
private:
    unsigned char* _image;
    unsigned long _imageSize;
    char* _language;
    int _OCREngineMode;
    char* _datapath;
    bool isCallTesseractOnly;
//    PIX* deskewedPix;
    STRING result;

public:
    OCRHandler(unsigned char* pImage, unsigned long imageSize,
            const char* pLanguage,
            const int pOCREngineMode, const char* pDatapath,
            const bool isCallTesseractOnly);

    virtual ~OCRHandler();

    bool ProcessOCR();
    STRING GetResult();
    PIX* GetDeskewedPix();

};

#endif	/* OCRHANDLER_H */

