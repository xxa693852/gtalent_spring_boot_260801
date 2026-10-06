package student.jia.gtalent_spring_boot_260801.service;

import org.springframework.stereotype.Service;

import com.linecorp.bot.webhook.model.Event;
import com.linecorp.bot.webhook.model.MessageContent;
import com.linecorp.bot.webhook.model.MessageEvent;
import com.linecorp.bot.webhook.model.TextMessageContent;
import com.linecorp.bot.webhook.model.UnsendEvent;

import com.linecorp.bot.webhook.model.GroupSource;
import com.linecorp.bot.webhook.model.RoomSource;
import com.linecorp.bot.webhook.model.UserSource;


@Service
public class LineWebhookService {

    private final LineReplyService lineReplyService;

    public LineWebhookService(LineReplyService lineReplyService) {
        this.lineReplyService = lineReplyService;
    }

    public void handleEvent(Event event) {

        System.out.println("收到 Event 類型：" + event.getClass().getSimpleName());
        //有人傳訊息
        if (event instanceof MessageEvent messageEvent) {
            handleMessageEvent(messageEvent);
        }
        //有人收回訊息
        if(event instanceof UnsendEvent unsendEvent){
           handleUnsendEvent(unsendEvent);
        }

    }

    private void handleMessageEvent(MessageEvent event) {
        MessageContent message = event.message();

        if (message instanceof TextMessageContent textMessage) {
            String lineUserId = event.source().userId();
            String text = textMessage.text();
            System.out.println("LINE webhook text message from " + lineUserId + ": " + text);
            lineReplyService.replyText(event.replyToken(), "收到：" + text);
        }
    }

    private void handleUnsendEvent(UnsendEvent event) {
      String messageId = event.unsend().messageId();

      System.out.println("有人收回訊息，messageId=" + messageId);

      if (event.source() instanceof GroupSource groupSource) {
          lineReplyService.pushText(
                  groupSource.groupId(),
                  "有人收回了一則訊息"
          );
          return;
      }

      if (event.source() instanceof RoomSource roomSource) {
          lineReplyService.pushText(
                  roomSource.roomId(),
                  "有人收回了一則訊息"
          );
          return;
      }

      if (event.source() instanceof UserSource userSource) {
          lineReplyService.pushText(
                  userSource.userId(),
                  "你收回了一則訊息"
          );
      }
  }
}