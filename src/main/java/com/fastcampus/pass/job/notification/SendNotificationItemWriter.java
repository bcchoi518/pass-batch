@Slf4j // Lombok 어노테이션: log 필드를 자동 생성하여 로그를 쉽게 남길 수 있게 해줌
@Component // Spring Bean으로 등록하여 DI 및 관리
public class SendNotificationItemWriter implements ItemWriter<NotificationEntity> {
    // NotificationRepository: 알림 엔티티를 DB에 저장/수정하는 JPA Repository
    private final NotificationRepository notificationRepository;
    // KakaoTalkMessageAdapter: 카카오톡 메시지 전송을 담당하는 어댑터(외부 API 연동)
    private final KakaoTalkMessageAdapter kakaoTalkMessageAdapter;

    // 생성자: DI를 통해 Repository와 Adapter를 주입받음
    public SendNotificationItemWriter(NotificationRepository notificationRepository, KakaoTalkMessageAdapter kakaoTalkMessageAdapter) {
        this.notificationRepository = notificationRepository; // 알림 DB 저장용 의존성 주입
        this.kakaoTalkMessageAdapter = kakaoTalkMessageAdapter; // 카카오톡 메시지 전송용 의존성 주입
    }

    /**
     * ItemWriter의 핵심 메서드. 배치에서 chunk 단위로 알림 엔티티 리스트를 받아 처리함.
     * @param notificationEntities 전송할 NotificationEntity 리스트
     * @throws Exception 예외 발생 시 배치가 실패로 처리됨
     */
    @Override
    public void write(List<? extends NotificationEntity> notificationEntities) throws Exception {
        int count = 0; // 성공적으로 전송된 알림 개수 카운트

        // 전달받은 알림 엔티티 리스트를 하나씩 순회하며 처리
        for (NotificationEntity notificationEntity : notificationEntities) {
            // 카카오톡 메시지 전송 시도 (성공 시 true 반환)
            boolean successful = kakaoTalkMessageAdapter.sendKakaoTalkMessage(
                    notificationEntity.getUuid(), // 수신자 UUID
                    notificationEntity.getText()  // 메시지 본문
            );

            if (successful) { // 메시지 전송 성공 시
                notificationEntity.setSent(true); // 발송 여부 true로 변경
                notificationEntity.setSentAt(LocalDateTime.now()); // 발송 일시 기록
                notificationRepository.save(notificationEntity); // DB에 저장(상태 반영)
                count ++; // 성공 카운트 증가
            }
        }
        // 전체 전송 결과를 info 레벨 로그로 출력
        log.info("SendNotificationItemWriter - write: 수업 전 알람 {}/{}건 전송 성공", count, notificationEntities.size());
    }
}