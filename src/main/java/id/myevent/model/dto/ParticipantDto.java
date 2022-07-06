package id.myevent.model.dto;

import lombok.Data;

@Data
public class ParticipantDto {
  private String name;
  private String email;
  private String phoneNumber;
  private Long dateEvent;
  private Long ticketId;
  private Long paymentId;
  private byte[] paymentProofPhoto;
  private String paymentPhotoName;
  private String paymentPhotoType;
}
