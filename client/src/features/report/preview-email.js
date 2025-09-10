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
                              <img src="data:image/png;base64,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" alt="Moment" style="width: 30px; height: 30px; border-radius: 50%; vertical-align: middle;">
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
