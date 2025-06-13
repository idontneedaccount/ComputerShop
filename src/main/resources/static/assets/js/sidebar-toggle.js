document.addEventListener('DOMContentLoaded', function() {
    const toggleLists = document.querySelectorAll('.toggle-list');
    document.querySelectorAll('.product-specifications').forEach(list => {
        list.classList.add('collapsed');
    });
    toggleLists.forEach(list => {
        const title = list.querySelector('.title');
        const submenu = list.querySelector('.shop-submenu');
        
        if (title && submenu) {
            title.addEventListener('click', function(e) {
                e.preventDefault();

                // Toggle class 'collapsed'
                list.classList.toggle('collapsed');

                // Toggle class active
                list.classList.toggle('active');
            });
        }
    });
}); 