package kim.chopper.chat;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "chat_message")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    private String nickName;
    private String contents;
    private Date createDate;

    public Chat() {

    }

    public Chat(String nickName, String contents) {
        this.nickName = nickName;
        this.contents = contents;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return seq.equals(chat.seq) &&
                nickName.equals(chat.nickName) &&
                Objects.equals(contents, chat.contents) &&
                createDate.equals(chat.createDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seq, nickName, contents, createDate);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Chat.class.getSimpleName() + "[", "]")
                .add("seq=" + seq)
                .add("nickName='" + nickName + "'")
                .add("contents='" + contents + "'")
                .add("createDate=" + createDate)
                .toString();
    }
}
