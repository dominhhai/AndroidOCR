/* 
 * File:   OCRHandler.cpp
 * Author: dominhhai
 *
 * Created on March 27, 2013, 1:58 AM
 */

#include "OCRHandler.h"
#include <string.h>
#include <malloc.h>
#include <iostream>

using namespace std;

OCRHandler::OCRHandler(unsigned char* pImage,
        unsigned long imageSize, const char* pLanguage,
        const int pOCREngineMode, const char* pDatapath,
        const bool isCallTesseractOnly) {
    this->_image = pImage;
    this->_imageSize = imageSize;
    this->_language = (char*) malloc(strlen(pLanguage) + 1);
    strcpy(this->_language, pLanguage);
    this->_OCREngineMode = pOCREngineMode;
    this->_datapath = (char*) malloc(strlen(pDatapath) + 1);
    strcpy(this->_datapath, pDatapath);
    this->isCallTesseractOnly = isCallTesseractOnly;
    result = "";
}

OCRHandler::~OCRHandler() {

}

bool OCRHandler::ProcessOCR() {
    // init engine
    tesseract::PageSegMode pagesegmode = static_cast<tesseract::PageSegMode> (this->_OCREngineMode);
    tesseract::TessBaseAPI tessbaseapi;

    if (tessbaseapi.Init(this->_datapath, this->_language, tesseract::OEM_DEFAULT)) {
        result = "Could not initialize OCR engine.";
        return false;
    }

    if (tessbaseapi.GetPageSegMode() == tesseract::PSM_SINGLE_BLOCK) {
        tessbaseapi.SetPageSegMode(pagesegmode);
    }

    // read input image
    PIX *pixs;
    if ((pixs = pixReadMem(this->_image, this->_imageSize)) == NULL) {
        result = "Unsupported this image type.";
        return false;
    }
    // de-skew
    //     * /** Default range for sweep, will detect rotation of + or - 30 degrees. */
    //    public final static float SWEEP_RANGE = 30.0f;
    //
    //    /** Default sweep delta, reasonably accurate within 0.05 degrees. */
    //    public final static float SWEEP_DELTA = 5.0f;
    //
    //    /** Default sweep reduction, one-eighth the size of the original image. */
    //    public final static int SWEEP_REDUCTION = 8;
    //
    //    /** Default sweep reduction, one-fourth the size of the original image. */
    //    public final static int SEARCH_REDUCTION = 4;
    //
    //    /** Default search minimum delta, reasonably accurate within 0.05 degrees. */
    //    public final static float SEARCH_MIN_DELTA = 0.01f;

    if (!this->isCallTesseractOnly) {
        pixs = pixDeskewGeneral(pixs, 0, 85.0f, 5.0f, 0, 0, NULL, NULL);
    }

    bool success = true;

    success &= tessbaseapi.ProcessPage(pixs, 0, NULL, NULL, 0, &this->result);

    // free memory
    pixDestroy(&pixs);
    tessbaseapi.End();

    return success;
}

STRING OCRHandler::GetResult() {
    return this->result;
}

PIX* OCRHandler::GetDeskewedPix() {
    return NULL;
}
