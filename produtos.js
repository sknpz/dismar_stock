    function render(list=products){
      if(!list.length){
        tableContainer.innerHTML = '';
        empty.hidden = false;
        return;
      }
      empty.hidden = true;
      const rows = list.map(p=>`
        <tr>
          <td>${p.name}</td>
          <td><span class="tag">${p.sku||'-'}</span></td>
          <td>${p.category||'-'}</td>
          <td>${p.qty}</td>
          <td>${formatMoney(Number(p.price))}</td>
        </tr>`).join('');

      tableContainer.innerHTML = `
        <table>
          <thead>
            <tr>
              <th>Produto</th>
              <th>SKU</th>
              <th>Categoria</th>
              <th>Qtd</th>
              <th>Preço</th>
            </tr>
          </thead>
          <tbody>${rows}</tbody>
        </table>`;
    }

    q.addEventListener('input', ()=>{
      const term = q.value.trim().toLowerCase();
      const filtered = products.filter(p=>[p.name,p.sku,p.category].join(' ').toLowerCase().includes(term));
      render(filtered);
    });

    render();

    function formatMoney(v){
  const cfg = JSON.parse(localStorage.getItem("dismar_cfg_v1") || "{}");
  const currency = cfg.currency || "BRL"; // padrão BRL
  const locales = {
    "BRL": "pt-BR",
    "USD": "en-US",
    "EUR": "de-DE"
  };
  return v.toLocaleString(locales[currency] || "pt-BR", {
    style: "currency",
    currency: currency
  });
}
