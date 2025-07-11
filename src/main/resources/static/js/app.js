// Cache global para evitar requisições desnecessárias
const AppCache = {
    data: new Map(),
    timestamps: new Map(),
    CACHE_DURATION: 5 * 60 * 1000, // 5 minutos
    
    set(key, data) {
        this.data.set(key, data);
        this.timestamps.set(key, Date.now());
    },
    
    get(key) {
        const timestamp = this.timestamps.get(key);
        if (!timestamp || Date.now() - timestamp > this.CACHE_DURATION) {
            this.data.delete(key);
            this.timestamps.delete(key);
            return null;
        }
        return this.data.get(key);
    },
    
    invalidate(key) {
        this.data.delete(key);
        this.timestamps.delete(key);
    },
    
    clear() {
        this.data.clear();
        this.timestamps.clear();
    }
};

// Função global de refresh
function refreshData(endpoint, updateCallback, buttonId = 'refreshBtn') {
    const button = document.getElementById(buttonId);
    const icon = button?.querySelector('i');
    
    if (button?.disabled) return;
    
    // Visual feedback
    if (button) button.disabled = true;
    if (icon) icon.classList.add('fa-spin');
    
    fetch(endpoint, {
        method: 'GET',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => response.json())
    .then(data => {
        AppCache.set(endpoint, data);
        updateCallback(data);
    })
    .catch(error => {
        console.error('Erro ao atualizar:', error);
    })
    .finally(() => {
        setTimeout(() => {
            if (button) button.disabled = false;
            if (icon) icon.classList.remove('fa-spin');
        }, 1000);
    });
}

// Toggle sidebar mobile
function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    sidebar?.classList.toggle('show');
}

// Invalidar cache quando dados são modificados
function invalidateCache() {
    AppCache.clear();
}

// Event listeners
document.addEventListener('DOMContentLoaded', function() {
    // Invalidar cache ao submeter formulários
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', invalidateCache);
    });
    
    // Fechar sidebar ao clicar fora (mobile)
    document.addEventListener('click', function(e) {
        const sidebar = document.querySelector('.sidebar');
        const toggleBtn = document.querySelector('.navbar-toggler');
        
        if (window.innerWidth <= 767 && 
            sidebar?.classList.contains('show') && 
            !sidebar.contains(e.target) && 
            !toggleBtn?.contains(e.target)) {
            sidebar.classList.remove('show');
        }
    });
});