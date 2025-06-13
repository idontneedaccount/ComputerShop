document.addEventListener('DOMContentLoaded', function() {
    let activeFilters = {
        category: 'all',
        brand: 'all',
        cpu: 'all',
        ram: 'all',
        ssd: 'all',
        vga: 'all',
        screen: 'all'
    };
    document.querySelectorAll('.shop-submenu a').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const filterType = this.getAttribute('data-filter-type');
            const filterValue = this.getAttribute('data-filter-value');
            if (filterType && filterValue) {
                activeFilters[filterType] = filterValue;
                const parentUl = this.closest('ul');
                parentUl.querySelectorAll('li').forEach(li => li.classList.remove('chosen'));
                this.closest('li').classList.add('chosen');
                filterProducts();
            }
        });
    });
    document.querySelector('.axil-btn.btn-bg-primary').addEventListener('click', function() {
        Object.keys(activeFilters).forEach(key => {
            activeFilters[key] = 'all';
        });
        document.querySelectorAll('.shop-submenu li').forEach(li => li.classList.remove('chosen'));
        document.querySelectorAll('.shop-submenu li:first-child').forEach(li => li.classList.add('chosen'));
        filterProducts();
    });

    function filterProducts() {
        const queryParams = new URLSearchParams();
        Object.keys(activeFilters).forEach(key => {
            if (activeFilters[key] !== 'all') {
                queryParams.append(key, activeFilters[key]);
            }
        });
        const filterUrl = '/user/shopping-page?' + queryParams.toString();
        window.location.href = filterUrl;
    }
}); 