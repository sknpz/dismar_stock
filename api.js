    // Cliente da API REST do Dismar Stock (backend/ em Java + Spring Boot)

    // Endereço da API. Pode ser sobrescrito definindo window.DISMAR_API_URL antes de carregar este arquivo.
    const API_BASE_URL = window.DISMAR_API_URL || 'http://localhost:8080';

    // Erro devolvido pela API no formato Problem Details (RFC 9457)
    class ApiError extends Error {
      constructor(message, status, fieldErrors = {}) {
        super(message);
        this.status = status;
        this.fieldErrors = fieldErrors;
      }
    }

    const api = (() => {
      async function request(method, path, body) {
        let response;
        try {
          response = await fetch(API_BASE_URL + path, {
            method,
            headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
            body: body === undefined ? undefined : JSON.stringify(body)
          });
        } catch {
          // fetch só rejeita quando não há resposta: API fora do ar, endereço errado ou CORS bloqueado
          throw new ApiError(`Não foi possível conectar à API em ${API_BASE_URL}. Verifique se o backend está rodando.`, 0);
        }

        if (response.status === 204) return null;

        const data = await response.json().catch(() => null);
        if (!response.ok) {
          throw new ApiError(data?.detail || `A API respondeu com erro ${response.status}.`, response.status, data?.errors || {});
        }
        return data;
      }

      // Monta a query string ignorando parâmetros vazios: { search: '', page: 0 } → "?page=0"
      const query = (params = {}) => {
        const search = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
          if (value !== undefined && value !== null && value !== '') search.append(key, value);
        });
        const result = search.toString();
        return result ? `?${result}` : '';
      };

      return {
        categories: {
          list: () => request('GET', '/api/categories'),
          create: (name) => request('POST', '/api/categories', { name })
        },
        suppliers: {
          list: (search) => request('GET', `/api/suppliers${query({ search })}`),
          get: (id) => request('GET', `/api/suppliers/${id}`),
          create: (supplier) => request('POST', '/api/suppliers', supplier),
          update: (id, supplier) => request('PUT', `/api/suppliers/${id}`, supplier),
          remove: (id) => request('DELETE', `/api/suppliers/${id}`)
        },
        products: {
          list: (params) => request('GET', `/api/products${query(params)}`),
          get: (id) => request('GET', `/api/products/${id}`),
          create: (product) => request('POST', '/api/products', product),
          update: (id, product) => request('PUT', `/api/products/${id}`, product),
          remove: (id) => request('DELETE', `/api/products/${id}`),
          registerMovement: (id, movement) => request('POST', `/api/products/${id}/movements`, movement)
        },
        movements: {
          list: (params) => request('GET', `/api/movements${query(params)}`)
        },
        reports: {
          summary: () => request('GET', '/api/reports/summary'),
          topProducts: (limit) => request('GET', `/api/reports/top-products${query({ limit })}`)
        }
      };
    })();
