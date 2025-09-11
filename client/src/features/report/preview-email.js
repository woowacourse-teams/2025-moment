import fs from 'fs';

const generateHtml = () => {
  return `
  <!DOCTYPE html>
  <html lang="ko">
  <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>Moment 일일 리포트</title>
  </head>
  <body style="margin: 0; padding: 0; background-color: #ffffff; font-family: Arial, sans-serif, '맑은 고딕', 'Malgun Gothic';">
      <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" style="margin: 0 auto; max-width: 600px; background-color: #0F172A; width: 100%;">
          <!-- 로고 섹션 -->
          <tr>
              <td style="padding: 20px 40px;">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                      <tr>
                          <td style="vertical-align: middle; padding-right: 8px;">
                              <img src="https://connectingmoment.com/images/logo.webp" alt="Moment" style="width: 30px; height: 30px; border-radius: 50%; vertical-align: middle;">
                          </td>
                          <td style="vertical-align: middle;">
                              <span style="font-size: 18px; font-weight: 600; color: #ffffff;">Moment</span>
                          </td>
                      </tr>
                  </table>
              </td>
          </tr>
          
          <!-- 메인 헤더 -->
          <tr>
              <td style="color: #ffffff; padding: 60px 40px; text-align: center;">
                  <h1 style="font-size: 48px; font-weight: 700; margin: 0 0 20px 0;">%s님!</h1>
                  <p style="font-size: 20px; font-weight: 400; margin: 0; opacity: 0.9;">오늘 하루의 기록을 전해드립니다</p>
              </td>
          </tr>
          
          <!-- 컨텐츠 섹션 -->
          <tr>
              <td style="padding: 40px;">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="width: 100%; background-color: #334155; border-radius: 8px;">
                      <tr>
                          <td style="padding: 20px; text-align: center;">
                              <h2 style="font-size: 18px; font-weight: 600; color: #ffffff; margin: 0 0 12px 0; text-decoration: underline;">일일 모멘트 작성여부</h2>
                              <p style="font-size: 16px; color: #ffffff; margin: 0 0 20px 0; line-height: 1.6;">%s</p>
                              
                              <h2 style="font-size: 18px; font-weight: 600; color: #ffffff; margin: 0 0 12px 0; text-decoration: underline;">일일 코멘트 현황</h2>
                              <p style="font-size: 16px; color: #ffffff; margin: 0; line-height: 1.6;">%s</p>
                          </td>
                      </tr>
                  </table>
              </td>
          </tr>
          
          <!-- CTA 섹션 -->
          <tr>
              <td style="text-align: center; padding: 40px;">
                  <p style="font-size: 18px; color: #F1C40F; margin: 0 0 24px 0; font-weight: 500;">지금 바로 확인하러 가볼까요?</p>
                  <a href="https://connectingmoment.com" style="display: inline-block; background-color: #F1C40F; color: #ffffff; padding: 16px 32px; border-radius: 8px; text-decoration: none; font-size: 16px; font-weight: 600;">Moment 바로가기</a>
              </td>
          </tr>
          
          <!-- 푸터 -->
          <tr>
              <td style="text-align: center; padding: 20px; color: #ffffff; font-size: 12px; background-color: #0F172A;">
                  <p style="margin: 0 0 5px 0;">이 메일은 매일 저녁 7시에 발송됩니다.</p>
                  <p style="margin: 0;">© 2025 Moment. All rights reserved.</p>
              </td>
          </tr>
      </table>
  </body>
  </html>`;
};

const serverHtml = generateHtml();
fs.writeFileSync('server-template.html', serverHtml);
console.log('server-template.html 파일이 생성되었습니다!');
