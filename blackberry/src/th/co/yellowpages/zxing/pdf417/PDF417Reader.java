/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package th.co.yellowpages.zxing.pdf417;


import java.util.Hashtable;

import th.co.yellowpages.zxing.BarcodeFormat;
import th.co.yellowpages.zxing.BinaryBitmap;
import th.co.yellowpages.zxing.DecodeHintType;
import th.co.yellowpages.zxing.FormatException;
import th.co.yellowpages.zxing.NotFoundException;
import th.co.yellowpages.zxing.Reader;
import th.co.yellowpages.zxing.Result;
import th.co.yellowpages.zxing.ResultPoint;
import th.co.yellowpages.zxing.common.BitMatrix;
import th.co.yellowpages.zxing.common.DecoderResult;
import th.co.yellowpages.zxing.common.DetectorResult;
import th.co.yellowpages.zxing.pdf417.decoder.Decoder;
import th.co.yellowpages.zxing.pdf417.detector.Detector;
import th.co.yellowpages.zxing.qrcode.QRCodeReader;

/**
 * This implementation can detect and decode PDF417 codes in an image.
 *
 * @author SITA Lab (kevin.osullivan@sita.aero)
 */
public final class PDF417Reader implements Reader {

  private static final ResultPoint[] NO_POINTS = new ResultPoint[0];

  private final Decoder decoder = new Decoder();

  /**
   * Locates and decodes a PDF417 code in an image.
   *
   * @return a String representing the content encoded by the PDF417 code
   * @throws NotFoundException if a PDF417 code cannot be found,
   * @throws FormatException if a PDF417 cannot be decoded
   */
  public Result decode(BinaryBitmap image) throws NotFoundException, FormatException {
    return decode(image, null);
  }

  public Result decode(BinaryBitmap image, Hashtable hints)
      throws NotFoundException, FormatException {
    DecoderResult decoderResult;
    ResultPoint[] points;
    if (hints != null && hints.containsKey(DecodeHintType.PURE_BARCODE)) {
      BitMatrix bits = QRCodeReader.extractPureBits(image.getBlackMatrix());
      decoderResult = decoder.decode(bits);
      points = NO_POINTS;
    } else {
      DetectorResult detectorResult = new Detector(image).detect();
      decoderResult = decoder.decode(detectorResult.getBits());
      points = detectorResult.getPoints();
    }
    return new Result(decoderResult.getText(), decoderResult.getRawBytes(), points,
        BarcodeFormat.PDF417);
  }

  public void reset() {
    // do nothing
  }

}
