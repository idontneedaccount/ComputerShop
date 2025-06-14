/**
 * Shopping page enhancements for sorting and filtering
 * Works in conjunction with pagination.js
 */
class ShoppingEnhancements {
    constructor() {
        this.init();
    }
    
    init() {
        this.setupSortingHandler();
        this.setupFilterHandler();
    }
    
    setupSortingHandler() {
        const sortSelect = document.querySelector('.single-select');
        if (sortSelect) {
            sortSelect.addEventListener('change', (e) => {
                this.sortProducts(e.target.value);
            });
        } else {
            // Try alternative selectors
            const altSelectors = ['select', '.form-select', '.category-select select', '.single-select'];
            for (let selector of altSelectors) {
                const element = document.querySelector(selector);
                if (element) {
                    element.addEventListener('change', (e) => {
                        this.sortProducts(e.target.value);
                    });
                    break;
                }
            }
        }
    }
    
    sortProducts(sortType) {
        const productsContainer = document.querySelector('.row.row--15');
        if (!productsContainer) return;
        
        // Get all visible products (not hidden by filters)
        const products = Array.from(productsContainer.querySelectorAll('.col-xl-4.col-sm-6'));
        
        if (products.length === 0) return;
        
        let sortedProducts = [...products]; // Create a copy to avoid mutation
        
        switch (sortType) {
            case 'Sắp xếp theo tên':
                sortedProducts.sort((a, b) => {
                    try {
                        const nameA = a.querySelector('.title a')?.textContent?.trim().toLowerCase() || '';
                        const nameB = b.querySelector('.title a')?.textContent?.trim().toLowerCase() || '';
                        return nameA.localeCompare(nameB, 'vi', { sensitivity: 'base' });
                    } catch (error) {
                        return 0;
                    }
                });
                break;
                
            case 'Sắp xếp theo giá':
                sortedProducts.sort((a, b) => {
                    try {
                        const priceA = this.extractPrice(a.querySelector('.current-price')?.textContent || '0');
                        const priceB = this.extractPrice(b.querySelector('.current-price')?.textContent || '0');
                        return priceA - priceB;
                    } catch (error) {
                        return 0;
                    }
                });
                break;
                
            case 'Sắp xếp theo cũ nhất':
                // Reverse the current order
                sortedProducts.reverse();
                break;
                
            default: // 'Sắp xếp theo mới nhất'
                // Keep original order - no sorting needed
                break;
        }
        
        // Clear container and re-append sorted products
        const fragment = document.createDocumentFragment();
        sortedProducts.forEach(product => {
            fragment.appendChild(product);
        });
        
        productsContainer.innerHTML = '';
        productsContainer.appendChild(fragment);
        
        // Add a small delay before refreshing pagination to ensure DOM is updated
        setTimeout(() => {
            if (window.productPagination) {
                window.productPagination.refresh();
            }
        }, 100);
    }
    
    extractPrice(priceText) {
        // Extract numeric value from price text (e.g., "1,000,000 VNĐ" -> 1000000)
        if (!priceText) return 0;
        
        // Remove all non-digit characters and convert to number
        const numericValue = priceText.replace(/[^\d]/g, '');
        return parseInt(numericValue) || 0;
    }
    
    setupFilterHandler() {
        // Handle mobile filter toggle
        const filterToggle = document.querySelector('.filter-toggle');
        if (filterToggle) {
            filterToggle.addEventListener('click', () => {
                const sidebar = document.querySelector('.axil-shop-sidebar');
                if (sidebar) {
                    sidebar.classList.toggle('show-mobile');
                }
            });
        }
        
        // Handle category filters (if sidebar has filter options)
        const categoryFilters = document.querySelectorAll('.category-filter');
        categoryFilters.forEach(filter => {
            filter.addEventListener('change', () => {
                this.filterProducts();
            });
        });
    }
    
    filterProducts() {
        // This method can be expanded to handle category filtering
        // For now, it just refreshes pagination
        if (window.productPagination) {
            window.productPagination.refresh();
        }
    }
}

// Initialize enhancements when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(() => {
        window.shoppingEnhancements = new ShoppingEnhancements();
    }, 200);
});

// Export for global access
window.ShoppingEnhancements = ShoppingEnhancements; 