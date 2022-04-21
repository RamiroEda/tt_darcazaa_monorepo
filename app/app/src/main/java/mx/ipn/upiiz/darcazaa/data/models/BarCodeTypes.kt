package mx.ipn.upiiz.darcazaa.data.models

enum class BarcodeTypes(val codeName: String){
    QRCode("QR_CODE"),
    Barcode("CODE_128"),
    DataMatrix("DATA_MATRIX"),
    PDF417("PDF_417"),
    Barcode39("CODE_39"),
    Barcode93("CODE_93"),
    Aztec("AZTEC")
}