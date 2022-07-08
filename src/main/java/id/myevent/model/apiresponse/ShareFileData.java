package id.myevent.model.apiresponse;

import lombok.Data;

/**
 * Share File Model.
 */
@Data
public class ShareFileData {
  private String judul;
  private byte[] fileShare;
  private String fileShareType;
  private String fileName;
  private String link;
  private String message;
}
