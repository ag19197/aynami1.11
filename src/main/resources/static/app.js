document.getElementById('planForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const destination = document.getElementById('destination').value.trim();
  const day = parseInt(document.getElementById('day').value, 10);
  const budget = document.getElementById('budget').value.trim();
  const output = document.getElementById('output');

  output.textContent = "正在生成，请稍候……";

  try {
    const resp = await fetch('/api/plan', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ destination, day, budget })
    });

    if (!resp.ok) {
      const txt = await resp.text();
      output.textContent = `请求失败：${resp.status}\n${txt}`;
      return;
    }

    const text = await resp.text(); // server 直接返回原始 JSON 或文本
    // 如果服务器返回 JSON 字符串，试着格式化显示
    try {
      const json = JSON.parse(text);
      output.textContent = JSON.stringify(json, null, 2);
    } catch (e) {
      output.textContent = text;
    }
  } catch (err) {
    output.textContent = '网络错误：' + (err.message || err);
  }
});
