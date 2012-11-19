package ro.ui.pttdroid.codecs;

public class Speex {
	
	static {
		System.loadLibrary("speex_jni");
	}
	
	private static final int[] encodedSizes = {6, 10, 15, 20, 20, 28, 28, 38, 38, 46, 62};
	
	public static int getEncodedSize(int quality) {
		return encodedSizes[quality];
	}

	public static native void open(int quality);
    public static native int decode(byte[] in, int length, short[] out);
    public static native int encode(short[] in, byte[] out);
    public static native void close();	
	
}
