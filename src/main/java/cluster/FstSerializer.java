package cluster;

import akka.serialization.JSerializer;
import org.nustaq.serialization.FSTConfiguration;

public class FstSerializer extends JSerializer {
    private static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    @Override
    public int identifier() {
        return 428442;
    }

    @Override
    public byte[] toBinary(Object o) {
        return conf.asByteArray(o);
    }

    @Override
    public Object fromBinaryJava(byte[] bytes, Class<?> manifest) {
        return conf.asObject(bytes);
    }

    @Override
    public boolean includeManifest() {
        return false;
    }
}
