document.getElementById('planForm').addEventListener('submit', async (e) => {
  e.preventDefault();

  const query = document.getElementById('query').value.trim();
  const outputEl = document.getElementById('output');
  const mapEl = document.getElementById('map');
  const resultDiv = document.getElementById('result');

  // 显示加载状态
  outputEl.style.display = 'block';
  mapEl.style.display = 'none';
  outputEl.textContent = '正在生成，请稍候……';

  try {
    // 发送请求到后端
    const resp = await fetch('/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        query: query,
        user: 'rei',
        conversationId: ''
      })
    });

    if (!resp.ok) {
      const txt = await resp.text();
      outputEl.textContent = `请求失败：${resp.status}\n${txt}`;
      return;
    }

    const data = await resp.json();  // 解析为 ChatResponse

    if (data.plan) {
      // 有结构化行程，隐藏文本输出，显示地图并渲染
      outputEl.style.display = 'none';
      mapEl.style.display = 'block';
      renderMap(data.plan);
    } else {
      // 无 plan，显示普通文本（可能是错误或普通回答）
      mapEl.style.display = 'none';
      outputEl.style.display = 'block';
      outputEl.textContent = data.answer || '无返回内容';
    }
  } catch (err) {
    outputEl.textContent = '网络错误：' + (err.message || err);
  }
});

/**
 * 使用 Leaflet 渲染地图
 * @param {Object} plan - TripPlan 对象
 */
function renderMap(plan) {
  const mapEl = document.getElementById('map');

  // 如果已存在地图实例，先销毁（避免重复初始化）
  if (window.tripMap) {
    window.tripMap.remove();
  }

  // 默认视图（东京塔坐标作为备选）
  let initLat = 35.6895, initLng = 139.6917;
  const days = plan.days || [];
  const allLocations = days.flatMap(day => day.locations || []);

  // 寻找第一个有效坐标
  const firstValid = allLocations.find(loc => loc.latitude && loc.longitude && loc.latitude !== 0 && loc.longitude !== 0);
  if (firstValid) {
    initLat = firstValid.latitude;
    initLng = firstValid.longitude;
  }

  // 初始化地图
  const map = L.map(mapEl).setView([initLat, initLng], 12);
  window.tripMap = map;  // 存储以便后续销毁

  // 添加 OpenStreetMap 底图
  L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>, &copy; CartoDB'
  }).addTo(map);

  const points = [];  // 用于绘制路线

  // 遍历所有地点的所有天
  days.forEach(day => {
    (day.locations || []).forEach(loc => {
      if (loc.latitude && loc.longitude && loc.latitude !== 0 && loc.longitude !== 0) {
        // 创建标记
        const marker = L.marker([loc.latitude, loc.longitude]).addTo(map);

        // 构造弹窗内容
        let popupContent = `<b>${escapeHtml(loc.name)}</b><br>${escapeHtml(loc.description)}`;

        // 如果有现实照片或动漫照片，添加图片对比
        if (loc.real_photo || loc.anime_photo) {
          popupContent += '<div style="display:flex; gap:10px; margin-top:5px; flex-wrap:wrap;">';
          if (loc.real_photo) {
            popupContent += `<img src="${loc.real_photo}" alt="现实照片" style="max-width:150px; max-height:100px; object-fit:cover;" referrerpolicy="no-referrer" onerror="this.style.display='none'">`;
          }
          if (loc.anime_photo) {
            popupContent += `<img src="${loc.anime_photo}" alt="动漫照片" style="max-width:150px; max-height:100px; object-fit:cover;" referrerpolicy="no-referrer" onerror="this.style.display='none'">`;
          }
          popupContent += '</div>';
        }

        marker.bindPopup(popupContent);

        // 记录点用于连线（按数据顺序）
        points.push([loc.latitude, loc.longitude]);
      }
    });
  });

  // 绘制路线（如果有至少两个点）
  if (points.length >= 2) {
    const polyline = L.polyline(points, { color: 'red', weight: 3 }).addTo(map);
    // 调整地图边界以包含所有点
    map.fitBounds(polyline.getBounds());
  } else if (points.length === 1) {
    map.setView(points[0], 14);
  }
}

/**
 * 简单的转义HTML特殊字符，防止XSS
 * @param {string} text
 * @returns {string}
 */
function escapeHtml(text) {
  if (!text) return '';
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  return text.replace(/[&<>"']/g, function(m) { return map[m]; });
}