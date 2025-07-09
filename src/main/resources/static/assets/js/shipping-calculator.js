/**
 * Simple Shipping Calculator using OpenRouteService API
 * Single shipping method only
 */

class ShippingCalculator {
    constructor() {
        this.orsApiKey = 'eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjU5NmI5NzNiZjc4ZTRmYWM5ZDRkZDA1MGI0OGVlNjliIiwiaCI6Im11cm11cjY0In0=';
        this.storeCoordinates = [105.5255, 21.0133]; // FPT Hoa Lac
        
        // Simple shipping config
        this.baseFee = 30000;           // 30,000đ phí cơ bản
        this.perKm = 2000;              // 2,000đ/km
        this.freeShippingThreshold = 20; // Miễn phí trong 20km
        
        this.currentDistance = 0;
        this.subtotal = 0;
        
        this.init();
    }

    init() {
        document.addEventListener('DOMContentLoaded', () => {
            this.setupEventListeners();
            this.getSubtotal();
            this.addTestButton();
        });
    }

    setupEventListeners() {
        const addressInput = document.getElementById('address');
        const districtInput = document.getElementById('district');
        const wardInput = document.getElementById('ward');
        const regionInput = document.querySelector('select[name="region"]');

        if (addressInput && districtInput && regionInput) {
            let timeoutId;
            const calculateWithDebounce = () => {
                clearTimeout(timeoutId);
                timeoutId = setTimeout(() => this.calculateDistance(), 1500);
            };

            addressInput.addEventListener('input', calculateWithDebounce);
            districtInput.addEventListener('input', calculateWithDebounce);
            if (wardInput) wardInput.addEventListener('input', calculateWithDebounce);
            regionInput.addEventListener('change', calculateWithDebounce);
        }
    }

    getSubtotal() {
        // Get ORIGINAL subtotal from backend, never change this
        const subtotalElement = document.getElementById('subtotal-display');
        if (subtotalElement) {
            const subtotalText = subtotalElement.textContent.replace(/[^\d]/g, '');
            this.subtotal = parseInt(subtotalText) || 0;
            console.log('✅ Original subtotal loaded:', this.subtotal);
            
            // Make sure subtotal display never changes
            subtotalElement.textContent = this.formatCurrency(this.subtotal, false);
        }
    }

    async calculateDistance() {
        const address = document.getElementById('address')?.value;
        const district = document.getElementById('district')?.value;
        const ward = document.getElementById('ward')?.value;
        const region = document.querySelector('select[name="region"]')?.value;

        if (!address || !district || !region) {
            this.hideShippingInfo();
            return;
        }

        // Build full address
        let customerAddress = address;
        if (ward) customerAddress += `, ${ward}`;
        customerAddress += `, ${district}, ${region}, Việt Nam`;
        
        try {
            this.showLoadingState();
            console.log('Calculating distance for:', customerAddress);
            
            const distance = await this.getDistanceFromOpenRoute(customerAddress);
            
            if (distance > 0) {
                this.currentDistance = distance;
                this.updateShippingDisplay(distance);
                this.showShippingInfo();
            } else {
                this.showError('Không thể tính toán khoảng cách. Vui lòng kiểm tra lại địa chỉ.');
            }
        } catch (error) {
            console.error('Error calculating distance:', error);
            // Fallback to estimated distance
            const fallbackDistance = this.getFallbackDistance(region, district);
            if (fallbackDistance > 0) {
                console.log('Using fallback distance:', fallbackDistance);
                this.currentDistance = fallbackDistance;
                this.updateShippingDisplay(fallbackDistance);
                this.showShippingInfo();
                this.showWarning('Sử dụng khoảng cách ước tính.');
            } else {
                this.showError('Có lỗi xảy ra khi tính toán khoảng cách.');
            }
        }
    }

    async getDistanceFromOpenRoute(destination) {
        try {
            // Geocode destination
            const destCoords = await this.geocodeAddress(destination);
            if (!destCoords) throw new Error('Cannot geocode destination');
            
            // Calculate route
            const routeResponse = await fetch('https://api.openrouteservice.org/v2/directions/driving-car', {
                method: 'POST',
                headers: {
                    'Authorization': this.orsApiKey,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    coordinates: [this.storeCoordinates, destCoords],
                    format: 'json'
                })
            });

            if (!routeResponse.ok) {
                throw new Error(`Route API error: ${routeResponse.status}`);
            }

            const routeData = await routeResponse.json();
            
            if (routeData.routes && routeData.routes.length > 0) {
                const distanceInMeters = routeData.routes[0].summary.distance;
                return Math.round(distanceInMeters / 1000 * 10) / 10;
            } else {
                throw new Error('No route found');
            }
        } catch (error) {
            throw error;
        }
    }

    async geocodeAddress(address) {
        try {
            const geocodeUrl = `https://api.openrouteservice.org/geocode/search?api_key=${this.orsApiKey}&text=${encodeURIComponent(address)}&boundary.country=VN&size=1`;
            
            const response = await fetch(geocodeUrl);
            if (!response.ok) throw new Error(`Geocoding error: ${response.status}`);
            
            const data = await response.json();
            
            if (data.features && data.features.length > 0) {
                const coords = data.features[0].geometry.coordinates;
                return [coords[0], coords[1]]; // [longitude, latitude]
            }
            
            return null;
        } catch (error) {
            throw error;
        }
    }

    getFallbackDistance(province, district) {
        const distanceTable = {
            'Hà Nội': 25, 'Hồ Chí Minh': 1760, 'Đà Nẵng': 800,
            'Hải Phòng': 120, 'Cần Thơ': 1900, 'Bắc Ninh': 60,
            'Bắc Giang': 80, 'Hải Dương': 90, 'Vĩnh Phúc': 40
        };

        let baseDistance = distanceTable[province] || 500;

        // Special handling for Hanoi districts
        if (province === 'Hà Nội') {
            const hanoiDistricts = {
                'Ba Đình': 25, 'Hoàn Kiếm': 25, 'Cầu Giấy': 23,
                'Đống Đa': 25, 'Thanh Xuân': 35, 'Thạch Thất': 5
            };
            
            for (const [districtName, adjustment] of Object.entries(hanoiDistricts)) {
                if (district && district.toLowerCase().includes(districtName.toLowerCase())) {
                    baseDistance = adjustment;
                    break;
                }
            }
        }

        return baseDistance;
    }

    calculateShippingFee(distance) {
        if (distance <= this.freeShippingThreshold) {
            return 0;
        }
        
        const fee = this.baseFee + (distance * this.perKm);
        return Math.round(fee / 1000) * 1000; // Round to nearest 1000
    }

    getEstimatedDeliveryTime(distance) {
        if (distance <= 50) return 2;
        if (distance <= 500) return 4;
        if (distance <= 1000) return 6;
        return 8;
    }

    updateShippingDisplay(distance) {
        // Update distance display
        const distanceDisplay = document.getElementById('distance-display');
        if (distanceDisplay) {
            distanceDisplay.textContent = distance.toFixed(1);
        }

        // Update delivery time
        const deliveryTime = document.getElementById('delivery-time');
        if (deliveryTime) {
            const days = this.getEstimatedDeliveryTime(distance);
            deliveryTime.textContent = `${days} ngày`;
        }

        // Calculate shipping fee
        const shippingFee = this.calculateShippingFee(distance);
        
        // 🔧 FIX: Update fee display correctly
        const feeElement = document.getElementById('standard-fee');
        if (feeElement) {
            if (shippingFee === 0) {
                feeElement.textContent = ' - Miễn phí';
                feeElement.style.color = '#28a745';
            } else {
                feeElement.textContent = ` - ${this.formatCurrency(shippingFee)}`;
                feeElement.style.color = '#dc3545'; // Red for paid shipping
            }
        }

        // 🔧 FIX: Update shipping fee row properly
        const shippingFeeRow = document.getElementById('shipping-fee-row');
        const currentShippingFee = document.getElementById('current-shipping-fee');
        if (shippingFeeRow && currentShippingFee) {
            // Always show shipping fee row when distance is calculated
            shippingFeeRow.style.display = 'table-row';
            
            if (shippingFee > 0) {
                currentShippingFee.textContent = this.formatCurrency(shippingFee, false);
                currentShippingFee.style.color = '#dc3545';
            } else {
                currentShippingFee.textContent = 'Miễn phí';
                currentShippingFee.style.color = '#28a745';
            }
        }

        // 🔧 FIX: Keep subtotal unchanged, only update final total
        // DON'T touch subtotal display, only update final total
        const finalTotal = document.getElementById('final-total');
        if (finalTotal) {
            const newTotal = this.subtotal + shippingFee;
            finalTotal.textContent = this.formatCurrency(newTotal, false);
            console.log(`✅ Correct calculation: Subtotal(${this.subtotal}) + Shipping(${shippingFee}) = Total(${newTotal})`);
        }

        // Update hidden fields
        const distanceValue = document.getElementById('distance-value');
        const shippingFeeValue = document.getElementById('shipping-fee-value');
        if (distanceValue) distanceValue.value = distance;
        if (shippingFeeValue) shippingFeeValue.value = shippingFee;
    }

    showShippingInfo() {
        const shippingInfo = document.getElementById('shipping-info');
        if (shippingInfo) {
            shippingInfo.style.display = 'block';
        }
    }

    hideShippingInfo() {
        const shippingInfo = document.getElementById('shipping-info');
        const shippingFeeRow = document.getElementById('shipping-fee-row');
        
        if (shippingInfo) shippingInfo.style.display = 'none';
        if (shippingFeeRow) shippingFeeRow.style.display = 'none';

        // Reset total
        const finalTotal = document.getElementById('final-total');
        if (finalTotal) {
            finalTotal.textContent = this.formatCurrency(this.subtotal, false);
        }

        // Reset fee display
        const feeElement = document.getElementById('standard-fee');
        if (feeElement) {
            feeElement.textContent = '- Tính phí khi có địa chỉ';
            feeElement.style.color = '#6c757d';
        }
    }

    showLoadingState() {
        const distanceDisplay = document.getElementById('distance-display');
        const deliveryTime = document.getElementById('delivery-time');
        
        if (distanceDisplay) distanceDisplay.textContent = 'Đang tính...';
        if (deliveryTime) deliveryTime.textContent = 'Đang tính...';
    }

    showError(message) {
        const shippingInfo = document.getElementById('shipping-info');
        if (shippingInfo) {
            shippingInfo.innerHTML = `
                <div class="alert alert-danger">
                    <h6><i class="fas fa-exclamation-triangle"></i> Lỗi</h6>
                    <p>${message}</p>
                </div>
            `;
            shippingInfo.style.display = 'block';
        }
    }

    showWarning(message) {
        const shippingInfo = document.getElementById('shipping-info');
        if (shippingInfo) {
            const existingContent = shippingInfo.innerHTML;
            shippingInfo.innerHTML = `
                <div class="alert alert-warning mb-2">
                    <small><i class="fas fa-info-circle"></i> ${message}</small>
                </div>
                ${existingContent}
            `;
        }
    }
}

// Initialize
function initMap() {
    console.log('Using OpenRouteService');
}

window.shippingCalculator = new ShippingCalculator(); 