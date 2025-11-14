    // Sistema de Dados
    const DATA_KEYS = {
      products: 'dismar_v2_products',
      suppliers: 'dismar_v2_suppliers',
      movements: 'dismar_v2_movements',
      config: 'dismar_v2_config'
    };

    let products = JSON.parse(localStorage.getItem(DATA_KEYS.products)) || [];
    let suppliers = JSON.parse(localStorage.getItem(DATA_KEYS.suppliers)) || [];
    let movements = JSON.parse(localStorage.getItem(DATA_KEYS.movements)) || [];
    let config = JSON.parse(localStorage.getItem(DATA_KEYS.config)) || { theme: 'light', currency: 'BRL' };

    // Aplicar tema
    if (config.theme === 'dark') {
      document.body.classList.add('dark');
      document.getElementById('themeIcon').textContent = '☀️';
    }

    // Funções Auxiliares
    const generateId = () => Math.random().toString(36).substr(2, 9);
    
    const formatMoney = (value) => {
      return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: config.currency || 'BRL'
      }).format(value);
    };

    const formatDate = (date) => {
      return new Date(date).toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    };

    const saveData = () => {
      localStorage.setItem(DATA_KEYS.products, JSON.stringify(products));
      localStorage.setItem(DATA_KEYS.suppliers, JSON.stringify(suppliers));
      localStorage.setItem(DATA_KEYS.movements, JSON.stringify(movements));
      localStorage.setItem(DATA_KEYS.config, JSON.stringify(config));
    };

    // Sistema de Navegação
    const pages = {
      dashboard: () => renderDashboard(),
      products: () => renderProducts(),
      suppliers: () => renderSuppliers(),
      movements: () => renderMovements(),
      reports: () => renderReports(),
      alerts: () => renderAlerts()
    };

    document.querySelectorAll('.nav-item').forEach(item => {
      item.addEventListener('click', (e) => {
        e.preventDefault();
        const page = e.currentTarget.dataset.page;
        
        document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
        e.currentTarget.classList.add('active');
        
        if (pages[page]) pages[page]();
      });
    });

    // Toggle Tema
    document.getElementById('themeToggle').addEventListener('click', () => {
      document.body.classList.toggle('dark');
      config.theme = document.body.classList.contains('dark') ? 'dark' : 'light';
      document.getElementById('themeIcon').textContent = config.theme === 'dark' ? '☀️' : '🌙';
      saveData();
    });

    // Dashboard
    function renderDashboard() {
      const totalProducts = products.length;
      const totalQty = products.reduce((sum, p) => sum + (p.qty || 0), 0);
      const totalValue = products.reduce((sum, p) => sum + ((p.qty || 0) * (p.price || 0)), 0);
      const lowStock = products.filter(p => p.qty <= (p.minStock || 10)).length;

      const recentMovements = movements.slice(0, 5);
      const topProducts = [...products].sort((a, b) => (b.qty * b.price) - (a.qty * a.price)).slice(0, 5);

      document.getElementById('mainContent').innerHTML = `
        <div class="top-bar">
          <div class="page-title">
            <h2>Dashboard</h2>
            <p>Visão geral do estoque</p>
          </div>
          <div class="top-actions">
            <div class="search-box">
              <span class="search-icon">🔍</span>
              <input type="text" placeholder="Buscar produtos..." id="quickSearch">
            </div>
            <button class="btn btn-primary" id="addProductBtn">
              <span>➕</span> Novo Produto
            </button>
          </div>
        </div>

        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">Total de Produtos</span>
              <div class="stat-icon" style="background: rgba(23, 162, 184, 0.1); color: var(--info);">📦</div>
            </div>
            <div class="stat-value">${totalProducts}</div>
            <span class="stat-change positive">↗ ${products.length > 0 ? '+5%' : '0%'} este mês</span>
          </div>

          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">Quantidade Total</span>
              <div class="stat-icon" style="background: rgba(40, 167, 69, 0.1); color: var(--success);">📊</div>
            </div>
            <div class="stat-value">${totalQty}</div>
            <span class="stat-change positive">↗ +12% este mês</span>
          </div>

          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">Valor Total</span>
              <div class="stat-icon" style="background: rgba(155, 0, 0, 0.1); color: var(--primary);">💰</div>
            </div>
            <div class="stat-value">${formatMoney(totalValue)}</div>
            <span class="stat-change positive">↗ +8% este mês</span>
          </div>

          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">Estoque Baixo</span>
              <div class="stat-icon" style="background: rgba(255, 193, 7, 0.1); color: var(--warning);">⚠️</div>
            </div>
            <div class="stat-value">${lowStock}</div>
            <span class="stat-change ${lowStock > 0 ? 'negative' : 'positive'}">${lowStock > 0 ? '⚠️ Atenção necessária' : '✓ Tudo OK'}</span>
          </div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px;">
          <div class="card">
            <div class="card-header">
              <h3 class="card-title">Produtos Mais Valiosos</h3>
            </div>
            ${topProducts.length > 0 ? `
              <table>
                <thead>
                  <tr>
                    <th>Produto</th>
                    <th>Qtd</th>
                    <th>Valor Total</th>
                  </tr>
                </thead>
                <tbody>
                  ${topProducts.map(p => `
                    <tr>
                      <td>
                        <strong>${p.name}</strong><br>
                        <small style="color: var(--text-muted);">${p.category || 'Sem categoria'}</small>
                      </td>
                      <td>${p.qty}</td>
                      <td><strong>${formatMoney(p.qty * p.price)}</strong></td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            ` : '<div class="empty-state"><div class="empty-icon">📦</div><div class="empty-title">Nenhum produto cadastrado</div></div>'}
          </div>

          <div class="card">
            <div class="card-header">
              <h3 class="card-title">Movimentações Recentes</h3>
            </div>
            ${recentMovements.length > 0 ? `
              <table>
                <thead>
                  <tr>
                    <th>Tipo</th>
                    <th>Produto</th>
                    <th>Qtd</th>
                    <th>Data</th>
                  </tr>
                </thead>
                <tbody>
                  ${recentMovements.map(m => `
                    <tr>
                      <td><span class="badge ${m.type === 'entry' ? 'badge-success' : 'badge-danger'}">${m.type === 'entry' ? 'Entrada' : 'Saída'}</span></td>
                      <td>${m.productName}</td>
                      <td>${m.qty}</td>
                      <td><small>${formatDate(m.date)}</small></td>
                    </tr>
                  `).join('')}
                </tbody>
              </table>
            ` : '<div class="empty-state"><div class="empty-icon">🔄</div><div class="empty-title">Nenhuma movimentação registrada</div></div>'}
          </div>
        </div>
      `;

      document.getElementById('addProductBtn')?.addEventListener('click', openProductModal);
      document.getElementById('quickSearch')?.addEventListener('input', quickSearch);
    }

    // Produtos
    function renderProducts() {
      document.getElementById('mainContent').innerHTML = `
        <div class="top-bar">
          <div class="page-title">
            <h2>Produtos</h2>
            <p>Gerencie seu catálogo de produtos</p>
          </div>
          <div class="top-actions">
            <div class="search-box">
              <span class="search-icon">🔍</span>
              <input type="text" placeholder="Buscar produtos..." id="searchProducts">
            </div>
            <button class="btn btn-primary" id="addProductBtn">
              <span>➕</span> Novo Produto
            </button>
          </div>
        </div>

        <div class="filters">
          <button class="filter-btn active" data-filter="all">Todos</button>
          <button class="filter-btn" data-filter="lowStock">Estoque Baixo</button>
          <button class="filter-btn" data-filter="outOfStock">Sem Estoque</button>
        </div>

        <div class="card">
          <div id="productsTable"></div>
        </div>
      `;

      renderProductsTable();
      
      document.getElementById('addProductBtn').addEventListener('click', openProductModal);
      document.getElementById('searchProducts').addEventListener('input', (e) => {
        renderProductsTable(e.target.value);
      });

      document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
          document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
          e.target.classList.add('active');
          
          const filter = e.target.dataset.filter;
          let filtered = products;
          
          if (filter === 'lowStock') {
            filtered = products.filter(p => p.qty > 0 && p.qty <= (p.minStock || 10));
          } else if (filter === 'outOfStock') {
            filtered = products.filter(p => p.qty === 0);
          }
          
          renderProductsTable('', filtered);
        });
      });
    }

    function renderProductsTable(search = '', filtered = products) {
      const tableEl = document.getElementById('productsTable');
      
      let list = filtered;
      if (search) {
        const term = search.toLowerCase();
        list = filtered.filter(p => 
          p.name.toLowerCase().includes(term) ||
          (p.sku || '').toLowerCase().includes(term) ||
          (p.category || '').toLowerCase().includes(term)
        );
      }

      if (list.length === 0) {
        tableEl.innerHTML = '<div class="empty-state"><div class="empty-icon">📦</div><div class="empty-title">Nenhum produto encontrado</div><p>Adicione um novo produto para começar</p></div>';
        return;
      }

      tableEl.innerHTML = `
        <table>
          <thead>
            <tr>
              <th>Produto</th>
              <th>SKU</th>
              <th>Categoria</th>
              <th>Quantidade</th>
              <th>Preço Unit.</th>
              <th>Valor Total</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            ${list.map(p => {
              const stockStatus = p.qty === 0 ? 'danger' : p.qty <= (p.minStock || 10) ? 'warning' : 'success';
              const stockLabel = p.qty === 0 ? 'Sem Estoque' : p.qty <= (p.minStock || 10) ? 'Baixo' : 'OK';
              
              return `
                <tr>
                  <td>
                    <strong>${p.name}</strong><br>
                    ${p.supplier ? `<small style="color: var(--text-muted);">Fornecedor: ${suppliers.find(s => s.id === p.supplier)?.name || 'N/A'}</small>` : ''}
                  </td>
                  <td><span class="badge badge-info">${p.sku || '-'}</span></td>
                  <td>${p.category || '-'}</td>
                  <td><strong>${p.qty}</strong></td>
                  <td>${formatMoney(p.price)}</td>
                  <td><strong>${formatMoney(p.qty * p.price)}</strong></td>
                  <td><span class="badge badge-${stockStatus}">${stockLabel}</span></td>
                  <td>
                    <div class="action-btns">
                      <button class="btn-icon" onclick="adjustStock('${p.id}', 'entry')" title="Entrada">➕</button>
                      <button class="btn-icon" onclick="adjustStock('${p.id}', 'exit')" title="Saída">➖</button>
                      <button class="btn-icon" onclick="editProduct('${p.id}')" title="Editar">✏️</button>
                      <button class="btn-icon" onclick="deleteProduct('${p.id}')" title="Excluir">🗑️</button>
                    </div>
                  </td>
                </tr>
              `;
            }).join('')}
          </tbody>
        </table>
      `;
    }

    // Fornecedores
    function renderSuppliers() {
      document.getElementById('mainContent').innerHTML = `
        <div class="top-bar">
          <div class="page-title">
            <h2>Fornecedores</h2>
            <p>Gerencie seus fornecedores</p>
          </div>
          <div class="top-actions">
            <div class="search-box">
              <span class="search-icon">🔍</span>
              <input type="text" placeholder="Buscar fornecedores..." id="searchSuppliers">
            </div>
            <button class="btn btn-primary" id="addSupplierBtn">
              <span>➕</span> Novo Fornecedor
            </button>
          </div>
        </div>

        <div class="card">
          <div id="suppliersTable"></div>
        </div>
      `;

      renderSuppliersTable();
      
      document.getElementById('addSupplierBtn').addEventListener('click', openSupplierModal);
      document.getElementById('searchSuppliers').addEventListener('input', (e) => {
        renderSuppliersTable(e.target.value);
      });
    }

    function renderSuppliersTable(search = '') {
      const tableEl = document.getElementById('suppliersTable');
      
      let list = suppliers;
      if (search) {
        const term = search.toLowerCase();
        list = suppliers.filter(s => 
          s.name.toLowerCase().includes(term) ||
          (s.cnpj || '').toLowerCase().includes(term) ||
          (s.email || '').toLowerCase().includes(term)
        );
      }

      if (list.length === 0) {
        tableEl.innerHTML = '<div class="empty-state"><div class="empty-icon">🏢</div><div class="empty-title">Nenhum fornecedor encontrado</div><p>Adicione um novo fornecedor para começar</p></div>';
        return;
      }

      tableEl.innerHTML = `
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>CNPJ</th>
              <th>Telefone</th>
              <th>E-mail</th>
              <th>Produtos</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            ${list.map(s => {
              const productsCount = products.filter(p => p.supplier === s.id).length;
              return `
                <tr>
                  <td><strong>${s.name}</strong></td>
                  <td>${s.cnpj || '-'}</td>
                  <td>${s.phone || '-'}</td>
                  <td>${s.email || '-'}</td>
                  <td><span class="badge badge-info">${productsCount} produto${productsCount !== 1 ? 's' : ''}</span></td>
                  <td>
                    <div class="action-btns">
                      <button class="btn-icon" onclick="editSupplier('${s.id}')" title="Editar">✏️</button>
                      <button class="btn-icon" onclick="deleteSupplier('${s.id}')" title="Excluir">🗑️</button>
                    </div>
                  </td>
                </tr>
              `;
            }).join('')}
          </tbody>
        </table>
      `;
    }

    // Movimentações
    function renderMovements() {
      document.getElementById('mainContent').innerHTML = `
        <div class="top-bar">
          <div class="page-title">
            <h2>Movimentações</h2>
            <p>Histórico de entradas e saídas</p>
          </div>
        </div>

        <div class="filters">
          <button class="filter-btn active" data-filter="all">Todas</button>
          <button class="filter-btn" data-filter="entry">Entradas</button>
          <button class="filter-btn" data-filter="exit">Saídas</button>
        </div>

        <div class="card">
          <div id="movementsTable"></div>
        </div>
      `;

      renderMovementsTable();

      document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
          document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
          e.target.classList.add('active');
          
          const filter = e.target.dataset.filter;
          const filtered = filter === 'all' ? movements : movements.filter(m => m.type === filter);
          renderMovementsTable(filtered);
        });
      });
    }

    function renderMovementsTable(list = movements) {
      const tableEl = document.getElementById('movementsTable');

      if (list.length === 0) {
        tableEl.innerHTML = '<div class="empty-state"><div class="empty-icon">🔄</div><div class="empty-title">Nenhuma movimentação registrada</div></div>';
        return;
      }

      tableEl.innerHTML = `
        <table>
          <thead>
            <tr>
              <th>Data</th>
              <th>Tipo</th>
              <th>Produto</th>
              <th>Quantidade</th>
              <th>Observações</th>
            </tr>
          </thead>
          <tbody>
            ${list.map(m => `
              <tr>
                <td>${formatDate(m.date)}</td>
                <td><span class="badge ${m.type === 'entry' ? 'badge-success' : 'badge-danger'}">${m.type === 'entry' ? '↗ Entrada' : '↘ Saída'}</span></td>
                <td><strong>${m.productName}</strong></td>
                <td><strong>${m.qty}</strong></td>
                <td>${m.notes || '-'}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      `;
    }

    // Relatórios
    function renderReports() {
      const totalValue = products.reduce((sum, p) => sum + (p.qty * p.price), 0);
      const avgValue = products.length > 0 ? totalValue / products.length : 0;
      
      const categoriesMap = {};
      products.forEach(p => {
        const cat = p.category || 'Sem categoria';
        if (!categoriesMap[cat]) {
          categoriesMap[cat] = { count: 0, value: 0 };
        }
        categoriesMap[cat].count += p.qty;
        categoriesMap[cat].value += p.qty * p.price;
      });

      document.getElementById('mainContent').innerHTML = `
        <div class="top-bar">
          <div class="page-title">
            <h2>Relatórios</h2>
            <p>Análises e estatísticas do estoque</p>
          </div>
        </div>

        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">Valor Médio por Produto</span>
              <div class="stat-icon" style="background: rgba(155, 0, 0, 0.1); color: var(--primary);">💵</div>
            </div>
            <div class="stat-value">${formatMoney(avgValue)}</div>
          </div>

          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">Categorias Cadastradas</span>
              <div class="stat-icon" style="background: rgba(23, 162, 184, 0.1); color: var(--info);">📂</div>
            </div>
            <div class="stat-value">${Object.keys(categoriesMap).length}</div>
          </div>

          <div class="stat-card">
            <div class="stat-header">
              <span class="stat-title">Movimentações (mês)</span>
              <div class="stat-icon" style="background: rgba(40, 167, 69, 0.1); color: var(--success);">🔄</div>
            </div>
            <div class="stat-value">${movements.length}</div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h3 class="card-title">Valor por Categoria</h3>
          </div>
          ${Object.keys(categoriesMap).length > 0 ? `
            <table>
              <thead>
                <tr>
                  <th>Categoria</th>
                  <th>Quantidade Total</th>
                  <th>Valor Total</th>
                  <th>% do Estoque</th>
                </tr>
              </thead>
              <tbody>
                ${Object.entries(categoriesMap).sort((a, b) => b[1].value - a[1].value).map(([cat, data]) => {
                  const percentage = ((data.value / totalValue) * 100).toFixed(1);
                  return `
                    <tr>
                      <td><strong>${cat}</strong></td>
                      <td>${data.count}</td>
                      <td><strong>${formatMoney(data.value)}</strong></td>
                      <td>
                        <div style="display: flex; align-items: center; gap: 8px;">
                          <div style="flex: 1; height: 8px; background: var(--bg-light); border-radius: 4px; overflow: hidden;">
                            <div style="height: 100%; width: ${percentage}%; background: var(--primary);"></div>
                          </div>
                          <span style="font-weight: 600;">${percentage}%</span>
                        </div>
                      </td>
                    </tr>
                  `;
                }).join('')}
              </tbody>
            </table>
          ` : '<div class="empty-state"><div class="empty-icon">📊</div><div class="empty-title">Sem dados para exibir</div></div>'}
        </div>
      `;
    }

    // Alertas
    function renderAlerts() {
      const lowStock = products.filter(p => p.qty > 0 && p.qty <= (p.minStock || 10));
      const outOfStock = products.filter(p => p.qty === 0);

      document.getElementById('mainContent').innerHTML = `
        <div class="top-bar">
          <div class="page-title">
            <h2>Alertas</h2>
            <p>Produtos que requerem atenção</p>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h3 class="card-title">⚠️ Estoque Baixo (${lowStock.length})</h3>
          </div>
          ${lowStock.length > 0 ? `
            <table>
              <thead>
                <tr>
                  <th>Produto</th>
                  <th>Quantidade Atual</th>
                  <th>Estoque Mínimo</th>
                  <th>Ação</th>
                </tr>
              </thead>
              <tbody>
                ${lowStock.map(p => `
                  <tr>
                    <td><strong>${p.name}</strong></td>
                    <td><span class="badge badge-warning">${p.qty}</span></td>
                    <td>${p.minStock || 10}</td>
                    <td>
                      <button class="btn btn-primary" onclick="adjustStock('${p.id}', 'entry')" style="padding: 6px 12px; font-size: 12px;">
                        Adicionar Estoque
                      </button>
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          ` : '<div class="empty-state"><div class="empty-icon">✅</div><div class="empty-title">Nenhum produto com estoque baixo</div></div>'}
        </div>

        <div class="card">
          <div class="card-header">
            <h3 class="card-title">🚫 Sem Estoque (${outOfStock.length})</h3>
          </div>
          ${outOfStock.length > 0 ? `
            <table>
              <thead>
                <tr>
                  <th>Produto</th>
                  <th>Categoria</th>
                  <th>Última Atualização</th>
                  <th>Ação</th>
                </tr>
              </thead>
              <tbody>
                ${outOfStock.map(p => `
                  <tr>
                    <td><strong>${p.name}</strong></td>
                    <td>${p.category || '-'}</td>
                    <td>${p.lastUpdate ? formatDate(p.lastUpdate) : '-'}</td>
                    <td>
                      <button class="btn btn-primary" onclick="adjustStock('${p.id}', 'entry')" style="padding: 6px 12px; font-size: 12px;">
                        Repor Estoque
                      </button>
                    </td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          ` : '<div class="empty-state"><div class="empty-icon">✅</div><div class="empty-title">Nenhum produto sem estoque</div></div>'}
        </div>
      `;
    }

    // Modal Produto
    function openProductModal(productId = null) {
      const modal = document.getElementById('productModal');
      const form = document.getElementById('productForm');
      const title = document.getElementById('productModalTitle');
      
      // Preencher fornecedores
      const supplierSelect = form.supplier;
      supplierSelect.innerHTML = '<option value="">Selecione...</option>' + 
        suppliers.map(s => `<option value="${s.id}">${s.name}</option>`).join('');

      if (productId) {
        const product = products.find(p => p.id === productId);
        if (product) {
          title.textContent = 'Editar Produto';
          form.name.value = product.name;
          form.sku.value = product.sku || '';
          form.category.value = product.category || '';
          form.qty.value = product.qty;
          form.price.value = product.price;
          form.minStock.value = product.minStock || 10;
          form.supplier.value = product.supplier || '';
          form.dataset.editId = productId;
        }
      } else {
        title.textContent = 'Novo Produto';
        form.reset();
        delete form.dataset.editId;
      }

      modal.classList.add('open');
    }

    function closeProductModal() {
      document.getElementById('productModal').classList.remove('open');
      document.getElementById('productForm').reset();
    }

    document.getElementById('closeProductModal').addEventListener('click', closeProductModal);
    document.getElementById('cancelProduct').addEventListener('click', closeProductModal);

    document.getElementById('productForm').addEventListener('submit', (e) => {
      e.preventDefault();
      const form = e.target;
      
      const data = {
        id: form.dataset.editId || generateId(),
        name: form.name.value.trim(),
        sku: form.sku.value.trim(),
        category: form.category.value.trim(),
        qty: parseInt(form.qty.value),
        price: parseFloat(form.price.value),
        minStock: parseInt(form.minStock.value) || 10,
        supplier: form.supplier.value,
        lastUpdate: new Date().toISOString()
      };

      if (form.dataset.editId) {
        const index = products.findIndex(p => p.id === data.id);
        products[index] = data;
      } else {
        products.unshift(data);
      }

      saveData();
      closeProductModal();
      renderProducts();
    });

    // Modal Fornecedor
    function openSupplierModal(supplierId = null) {
      const modal = document.getElementById('supplierModal');
      const form = document.getElementById('supplierForm');
      const title = document.getElementById('supplierModalTitle');

      if (supplierId) {
        const supplier = suppliers.find(s => s.id === supplierId);
        if (supplier) {
          title.textContent = 'Editar Fornecedor';
          form.name.value = supplier.name;
          form.cnpj.value = supplier.cnpj || '';
          form.phone.value = supplier.phone || '';
          form.email.value = supplier.email || '';
          form.address.value = supplier.address || '';
          form.dataset.editId = supplierId;
        }
      } else {
        title.textContent = 'Novo Fornecedor';
        form.reset();
        delete form.dataset.editId;
      }

      modal.classList.add('open');
    }

    function closeSupplierModal() {
      document.getElementById('supplierModal').classList.remove('open');
      document.getElementById('supplierForm').reset();
    }

    document.getElementById('closeSupplierModal').addEventListener('click', closeSupplierModal);
    document.getElementById('cancelSupplier').addEventListener('click', closeSupplierModal);

    document.getElementById('supplierForm').addEventListener('submit', (e) => {
      e.preventDefault();
      const form = e.target;
      
      const data = {
        id: form.dataset.editId || generateId(),
        name: form.name.value.trim(),
        cnpj: form.cnpj.value.trim(),
        phone: form.phone.value.trim(),
        email: form.email.value.trim(),
        address: form.address.value.trim()
      };

      if (form.dataset.editId) {
        const index = suppliers.findIndex(s => s.id === data.id);
        suppliers[index] = data;
      } else {
        suppliers.unshift(data);
      }

      saveData();
      closeSupplierModal();
      renderSuppliers();
    });

    // Funções Globais
    window.editProduct = (id) => openProductModal(id);
    window.editSupplier = (id) => openSupplierModal(id);

    window.deleteProduct = (id) => {
      if (confirm('Deseja realmente excluir este produto?')) {
        products = products.filter(p => p.id !== id);
        saveData();
        renderProducts();
      }
    };

    window.deleteSupplier = (id) => {
      const hasProducts = products.some(p => p.supplier === id);
      if (hasProducts) {
        alert('Não é possível excluir este fornecedor pois existem produtos vinculados a ele.');
        return;
      }
      if (confirm('Deseja realmente excluir este fornecedor?')) {
        suppliers = suppliers.filter(s => s.id !== id);
        saveData();
        renderSuppliers();
      }
    };

    window.adjustStock = (id, type) => {
      const product = products.find(p => p.id === id);
      if (!product) return;

      const qty = parseInt(prompt(`${type === 'entry' ? 'Entrada' : 'Saída'} de estoque para ${product.name}:\n\nQuantidade:`));
      if (!qty || qty <= 0) return;

      const oldQty = product.qty;
      
      if (type === 'entry') {
        product.qty += qty;
      } else {
        if (product.qty < qty) {
          alert('Quantidade insuficiente em estoque!');
          return;
        }
        product.qty -= qty;
      }

      product.lastUpdate = new Date().toISOString();

      movements.unshift({
        id: generateId(),
        productId: product.id,
        productName: product.name,
        type: type,
        qty: qty,
        oldQty: oldQty,
        newQty: product.qty,
        date: new Date().toISOString(),
        notes: ''
      });

      saveData();
      renderProducts();
    };

    function quickSearch(e) {
      const term = e.target.value.toLowerCase();
      if (term.length < 2) return;

      const results = products.filter(p => 
        p.name.toLowerCase().includes(term) ||
        (p.sku || '').toLowerCase().includes(term)
      ).slice(0, 5);

      console.log('Resultados da busca:', results);
    }

    // Inicializar
    renderDashboard();