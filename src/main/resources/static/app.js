document.getElementById('planForm').addEventListener('submit', async (e) => {
  e.preventDefault();

  const query = document.getElementById('query').value.trim();
  const outputEl = document.getElementById('output');
  const mapEl = document.getElementById('map');

  // 显示加载状态
  outputEl.style.display = 'block';
  mapEl.style.display = 'none';
  outputEl.textContent = "正在生成，请稍候……";

  try {
    // 发送请求，固定 user 为 "rei"，conversationId 为空（新对话）
    const resp = await fetch('/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        query: query,
        user: 'rei',
        conversationId: ''  // 新对话留空
      })
    });

    if (!resp.ok) {
      const txt = await resp.text();
      outputEl.textContent = `请求失败：${resp.status}\n${txt}`;
      return;
    }

    const data = await resp.json();  // 解析为 ChatResponse 对象

    if (data.plan) {
      // 有结构化行程，隐藏文本输出，显示地图并渲染
      outputEl.style.display = 'none';
      mapEl.style.display = 'block';
      renderMap(data.plan);
    } else {
      // 无 plan，显示普通文本（可能是错误信息或普通回答）
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
 * @param {Object} plan - 符合 TripPlan 结构的对象
 */
function renderMap(plan) {
  const mapEl = document.getElementById('map');

  // 如果已存在地图实例，先销毁
  if (window.tripMap) {
    window.tripMap.remove();
  }

  // 默认视图（东京塔坐标）
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
  window.tripMap = map;

  // 添加底图
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  }).addTo(map);

  const points = [];

  // 遍历所有地点
  days.forEach(day => {
    (day.locations || []).forEach(loc => {
      if (loc.latitude && loc.longitude && loc.latitude !== 0 && loc.longitude !== 0) {
        const marker = L.marker([loc.latitude, loc.longitude]).addTo(map);

        // 构造弹窗内容
        let popupContent = `<b>${loc.name}</b><br>${loc.description}`;
        if (loc.real_photo || loc.anime_photo) {
          popupContent += '<div style="display:flex; gap:10px; margin-top:5px;">';
          if (loc.real_photo) {
            popupContent += `<img src="${loc.real_photo}" style="max-width:150px; max-height:100px; object-fit:cover;" referrerpolicy="no-referrer">`;
          }
          if (loc.anime_photo) {
            popupContent += `<img src="${loc.anime_photo}" style="max-width:150px; max-height:100px; object-fit:cover;" referrerpolicy="no-referrer">`;
          }
          popupContent += '</div>';
        }
        marker.bindPopup(popupContent);

        points.push([loc.latitude, loc.longitude]);
      }
    });
  });

  // 绘制路线
  if (points.length >= 2) {
    const polyline = L.polyline(points, { color: 'red', weight: 3 }).addTo(map);
    map.fitBounds(polyline.getBounds());
  } else if (points.length === 1) {
    map.setView(points[0], 14);
  }
}