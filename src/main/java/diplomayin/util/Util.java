package diplomayin.util;

import aca.proto.ChatMsg;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import diplomayin.client.RSA.RSA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.security.PublicKey;


public class Util {
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    public static boolean write(ByteBuffer byteBuffer, ChatMsg chatMsg, PublicKey publicKey) throws Exception {
        ChatMsg chatMsgCopy = null;
        if (publicKey != null) {
            chatMsgCopy = ChatMsg.newBuilder()
                    .setTime(chatMsg.getTime())
                    .setUserSentPrivateMessage(ChatMsg.UserSentPrivateMessage.newBuilder()
                            .setSender(chatMsg.getUserSentPrivateMessage().getSender())
                            .setMessage(ByteString.copyFrom(RSA.encrypt(chatMsg.getUserSentPrivateMessage().getMessage().toStringUtf8(), publicKey)))
                            .addAllReceiver(chatMsg.getUserSentPrivateMessage().getReceiverList()))
                    .build();
        }
        byte[] bytes;
        if (publicKey != null) bytes = chatMsgCopy.toByteArray();
        else bytes = chatMsg.toByteArray();
        int totalBytesForMessage = 4 + bytes.length;

        if (byteBuffer.remaining() < totalBytesForMessage) {
            return false;
        }

        byteBuffer.putInt(bytes.length);
        byteBuffer.put(bytes);
        return true;


//        byte[] bytes = chatMsg.toByteArray();
//        byteBuffer.putInt(bytes.length);
//        byteBuffer.put(bytes);


    }


    public static ChatMsg read(ByteBuffer byteBuffer) {


        while (true) {
            if (byteBuffer.remaining() <= 4) {
                continue;

            }

            byteBuffer.mark();
            int length = byteBuffer.getInt();

            if (byteBuffer.remaining() < length) {
                byteBuffer.reset();
                continue;
            }

            byte[] bytes = new byte[length];
            byteBuffer.get(bytes);

            try {
                return ChatMsg.parseFrom(bytes);
            } catch (InvalidProtocolBufferException e) {
                LOG.error("Invalid message");
                return null;
            }

        }
    }

    public static PublicKey readPublicKey(ByteBuffer byteBuffer) throws IOException, ClassNotFoundException {
        while (true) {
            if (byteBuffer.remaining() <= 4) {
                continue;

            }

            byteBuffer.mark();
            int length = byteBuffer.getInt();

            if (byteBuffer.remaining() < length) {
                byteBuffer.reset();
                continue;
            }

            byte[] bytes = new byte[length];
            byteBuffer.get(bytes);

            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInput in = new ObjectInputStream(bis);
            return (PublicKey) in.readObject();
        }
    }


}
