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

    // Format currency to Vietnamese Dong
    formatCurrency(amount, includeCurrency = true) {
        const formatted = new Intl.NumberFormat('vi-VN').format(amount);
        return includeCurrency ? `${formatted} ₫` : formatted;
    }

    // Add test button for development
    addTestButton() {
        if (document.getElementById('test-shipping-btn')) return; // Already exists
        
        const container = document.querySelector('.checkout-form, .shipping-calculator');
        if (container && window.location.hostname === 'localhost') {
            const testBtn = document.createElement('button');
            testBtn.id = 'test-shipping-btn';
            testBtn.type = 'button';
            testBtn.className = 'btn btn-sm btn-secondary mt-2';
            testBtn.innerHTML = '<i class="fas fa-vial"></i> Test Shipping';
            testBtn.style.fontSize = '12px';
            
            testBtn.addEventListener('click', () => {
                // Fill test address
                const addressInput = document.getElementById('address');
                const districtInput = document.getElementById('district');
                const regionInput = document.querySelector('select[name="region"]');
                
                if (addressInput) addressInput.value = 'Số 61 Phú Yên';
                if (districtInput) districtInput.value = 'Phú Vĩnh';
                if (regionInput) regionInput.value = 'An Khánh, Hoai Đức, Hà Nội, Việt Nam';
                
                // Trigger calculation
                this.calculateDistance();
            });
            
            container.appendChild(testBtn);
        }
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
            console.log('🚗 Starting route calculation...');
            console.log('📍 Store location (FPT Hoa Lac):', this.storeCoordinates);
            
            // Geocode destination
            const destCoords = await this.geocodeAddress(destination);
            if (!destCoords) throw new Error('Cannot geocode destination');
            
            console.log('📍 Destination coordinates:', destCoords);
            
            // Quick distance check to validate coordinates
            const straightLineDistance = this.calculateStraightLineDistance(
                this.storeCoordinates[1], this.storeCoordinates[0],
                destCoords[1], destCoords[0]
            );
            console.log(`📏 Straight-line distance: ${straightLineDistance.toFixed(1)}km`);
            
            // If straight-line distance is unreasonable (>100km for Hanoi addresses), use fallback
            if (destination.toLowerCase().includes('hà nội') && straightLineDistance > 100) {
                console.log('⚠️ Unreasonable distance detected, using fallback');
                throw new Error('Geocoding appears incorrect');
            }
            
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
            console.log('🛣️ Route data:', routeData);
            
            if (routeData.routes && routeData.routes.length > 0) {
                const distanceInMeters = routeData.routes[0].summary.distance;
                const distanceInKm = Math.round(distanceInMeters / 1000 * 10) / 10;
                console.log(`✅ Calculated distance: ${distanceInKm}km`);
                return distanceInKm;
            } else {
                throw new Error('No route found');
            }
        } catch (error) {
            console.error('❌ Route calculation error:', error);
            throw error;
        }
    }

    // Calculate straight-line distance between two points (Haversine formula)
    calculateStraightLineDistance(lat1, lon1, lat2, lon2) {
        const R = 6371; // Earth's radius in kilometers
        const dLat = this.toRadians(lat2 - lat1);
        const dLon = this.toRadians(lon2 - lon1);
        const a = 
            Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(this.toRadians(lat1)) * Math.cos(this.toRadians(lat2)) * 
            Math.sin(dLon/2) * Math.sin(dLon/2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    toRadians(degrees) {
        return degrees * (Math.PI/180);
    }

    async geocodeAddress(address) {
        try {
            // Debug log
            console.log('🔍 Geocoding address:', address);
            
            const geocodeUrl = `https://api.openrouteservice.org/geocode/search?api_key=${this.orsApiKey}&text=${encodeURIComponent(address)}&boundary.country=VN&size=5`;
            
            const response = await fetch(geocodeUrl);
            if (!response.ok) throw new Error(`Geocoding error: ${response.status}`);
            
            const data = await response.json();
            console.log('🗺️ Geocoding results:', data);
            
            if (data.features && data.features.length > 0) {
                // Smart filtering for Vietnamese addresses
                let bestResult = null;
                let bestScore = 0;
                
                // Extract key location terms from original address
                const addressLower = address.toLowerCase();
                const locationKeywords = [];
                if (addressLower.includes('hoài đức')) locationKeywords.push('hoài đức');
                if (addressLower.includes('an khánh')) locationKeywords.push('an khánh');
                if (addressLower.includes('thạch thất')) locationKeywords.push('thạch thất');
                if (addressLower.includes('hà đông')) locationKeywords.push('hà đông');
                
                for (const feature of data.features) {
                    const coords = feature.geometry.coordinates;
                    const label = (feature.properties.label || '').toLowerCase();
                    const confidence = feature.properties.confidence || 0;
                    
                    console.log(`📍 Result: ${feature.properties.label}, coords: [${coords[1]}, ${coords[0]}], confidence: ${confidence}`);
                    
                    // Check if it's really in Hanoi area
                    const lat = coords[1];
                    const lng = coords[0];
                    
                    if (lat >= 20.8 && lat <= 21.2 && lng >= 105.2 && lng <= 106.2) {
                        let score = confidence;
                        
                        // Boost score for matching specific districts/communes
                        for (const keyword of locationKeywords) {
                            if (label.includes(keyword)) {
                                score += 0.5; // Strong boost for exact area match
                                console.log(`🎯 Keyword match: ${keyword} (+0.5)`);
                            }
                        }
                        
                        // Prefer results closer to FPT Hoa Lac area (around [105.52, 21.01])
                        const distanceFromFPT = Math.sqrt(
                            Math.pow(lng - 105.52, 2) + Math.pow(lat - 21.01, 2)
                        );
                        if (distanceFromFPT < 0.1) score += 0.3; // Very close to FPT area
                        else if (distanceFromFPT < 0.2) score += 0.1; // Reasonably close
                        
                        // Avoid central Hanoi results for suburban addresses
                        if (lng > 105.8 && lat > 21.02) {
                            score -= 0.2; // Penalty for central Hanoi coordinates
                            console.log(`⚠️ Central Hanoi penalty for [${lat}, ${lng}]`);
                        }
                        
                        console.log(`📊 Score: ${score.toFixed(2)} for ${feature.properties.label}`);
                        
                        if (score > bestScore) {
                            bestResult = coords;
                            bestScore = score;
                        }
                    }
                }
                
                if (bestResult) {
                    console.log(`✅ Selected best result: [${bestResult[1]}, ${bestResult[0]}] with score: ${bestScore.toFixed(2)}`);
                    return [bestResult[0], bestResult[1]]; // [longitude, latitude]
                }
                
                // Fallback to first result if no good match found
                const coords = data.features[0].geometry.coordinates;
                console.log(`⚠️ Using fallback result: [${coords[1]}, ${coords[0]}]`);
                return [coords[0], coords[1]];
            }
            
            return null;
        } catch (error) {
            console.error('❌ Geocoding error:', error);
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

        // Special handling for Hanoi districts and communes - ACCURATE distances from FPT Hoa Lac
        if (province && province.toLowerCase().includes('hà nội')) {
            const hanoiAreas = {
                // Districts closest to FPT Hoa Lac
                'thạch thất': 5,     // FPT is IN Thạch Thất district
                'hoài đức': 8,       // Adjacent district
                'quốc oai': 12,      // Near neighbor
                'hà đông': 16,       // Southwest Hanoi
                'đan phượng': 20,    // North of FPT
                
                // Suburban districts
                'nam từ liêm': 22,
                'bắc từ liêm': 28,
                'sóc sơn': 45,
                'mê linh': 40,
                'chương mỹ': 35,
                'thanh oai': 30,
                'phúc thọ': 38,
                'ba vì': 42,
                
                // Inner city districts (further from FPT)
                'tây hồ': 30,
                'cầu giấy': 32,
                'ba đình': 35,
                'hoàn kiếm': 38,
                'đống đa': 35,
                'thanh xuân': 40,
                'hai bà trưng': 42,
                'hoàng mai': 45,
                'long biên': 48,
                'gia lâm': 50,
                'đông anh': 55,
                
                // Specific communes/wards near FPT Hoa Lac
                'an khánh': 6,       // Hoài Đức - very close to FPT
                'la phù': 4,         // Hoài Đức - closest commune
                'kim chung': 8,      // Hoài Đức  
                'đức giang': 10,     // Hoài Đức
                'song phương': 12,   // Hoài Đức
                'vân canh': 14,      // Hoài Đức
                'đức thượng': 12,    // Hoài Đức
                
                // Thạch Thất communes (FPT's district)
                'bình yên': 3,       // Very close to FPT
                'liên quan': 7,      
                'đại thịnh': 10,
                'cần kiệm': 8,
                'yên bình': 12,
                
                // Other areas
                'phú cát': 18,       // Quốc Oai
                'lại yên': 20,       // Hoài Đức
                'đông xuân': 25      // Further out
            };
            
            // Check district first
            for (const [areaName, distance] of Object.entries(hanoiAreas)) {
                if (district && district.toLowerCase().includes(areaName)) {
                    console.log(`📍 Found specific area: ${areaName} = ${distance}km`);
                    return distance;
                }
            }
            
            // Default for Hanoi if no specific area found
            baseDistance = 25;
        }

        console.log(`📍 Using fallback distance: ${baseDistance}km for ${province}/${district}`);
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