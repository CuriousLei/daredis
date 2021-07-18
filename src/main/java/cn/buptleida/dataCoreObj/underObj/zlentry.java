package cn.buptleida.dataCoreObj.underObj;

public class zlentry {
    //前置结点长度
    int prevrawlen;
    //记录前置结点长度的字节数
    int prevrawlensize;

    //编码值
    int encoding;
    //编码encoding的字节数
    int encodingSize;

    //头部长度
    int headerSize;

    //content的起始位置
    int contentPos;
    //content的字节数
    int contentSize;

    public int size() {
        return headerSize + contentSize;
    }
    public int startPos(){
        return contentPos - headerSize;
    }
    public int endPos(){
        return contentPos + contentSize;
    }
}
