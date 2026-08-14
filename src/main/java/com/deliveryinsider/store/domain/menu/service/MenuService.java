package com.deliveryinsider.store.domain.menu.service;

import com.deliveryinsider.store.domain.menu.entity.Menu;
import com.deliveryinsider.store.domain.menu.entity.MenuLossDismissal;
import com.deliveryinsider.store.domain.menu.mapper.MenuMapper;
import com.deliveryinsider.store.domain.menu.request.MenuCreateRequest;
import com.deliveryinsider.store.domain.menu.request.MenuLossDismissRequest;
import com.deliveryinsider.store.domain.menu.request.MenuUpdateRequest;
import com.deliveryinsider.store.domain.menu.response.MenuResponse;
import com.deliveryinsider.store.domain.menu.response.MenuLossDismissalResponse;
import com.deliveryinsider.store.domain.store.entity.Store;
import com.deliveryinsider.store.domain.store.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuMapper menuMapper;
    private final StoreMapper storeMapper;

    @Transactional(rollbackFor = Exception.class)
    public MenuResponse create(Long userId, MenuCreateRequest createReq) {
        Store store = getActiveStore(userId);

        Menu menu = Menu.builder()
                .storeId(store.getId())
                .menuName(createReq.menuName())
                .menuPrice(createReq.menuPrice())
                .menuCost(createReq.menuCost())
                .packagingFee(createReq.packagingFee())
                .expectedCookingTime(createReq.expectedCookingTime())
                .batchCapacity(createReq.batchCapacity())
                .build();

        int result = menuMapper.save(menu);

        if (result != 1) {
            throw new RuntimeException("메뉴 등록 중 문제가 발생했습니다.");
        }

        Menu savedMenu = menuMapper.findByIdAndStoreId(menu.getId(), store.getId());
        if (savedMenu == null) {
            throw new RuntimeException("등록된 메뉴를 조회할 수 없습니다.");
        }

        return toMenuResponse(savedMenu);
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> findAll(Long userId) {
        Store store = getActiveStore(userId);

        List<Menu> menus = menuMapper.findAllByStoreId(store.getId());

        return menus.stream()
                .map(this::toMenuResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MenuResponse findOne(Long userId, Long menuId) {
        Store store = getActiveStore(userId);

        Menu menu = menuMapper.findByIdAndStoreId(menuId, store.getId());

        if (menu == null) {
            throw new RuntimeException("메뉴를 찾을 수 없습니다.");
        }

        return toMenuResponse(menu);
    }

    @Transactional(rollbackFor = Exception.class)
    public MenuResponse update(Long userId, Long menuId, MenuUpdateRequest updateReq) {
        Store store = getActiveStore(userId);
        Menu currentMenu = menuMapper.findByIdAndStoreId(menuId, store.getId());

        if (currentMenu == null) {
            throw new RuntimeException("수정할 메뉴를 찾을 수 없습니다.");
        }

        Menu updateMenu = Menu.builder()
                .id(currentMenu.getId())
                .storeId(store.getId())
                .menuName(updateReq.menuName())
                .menuPrice(updateReq.menuPrice())
                .menuCost(updateReq.menuCost())
                .packagingFee(updateReq.packagingFee())
                .expectedCookingTime(updateReq.expectedCookingTime())
                .batchCapacity(updateReq.batchCapacity())
                .build();

        int result = menuMapper.update(updateMenu);

        if (result != 1) {
            throw new RuntimeException("메뉴 수정 중 문제가 발생했습니다.");
        }

        menuMapper.restoreLossDismissal(store.getId(), menuId);

        Menu updatedMenu = menuMapper.findByIdAndStoreId(menuId, store.getId());
        if (updatedMenu == null) {
            throw new RuntimeException("수정된 메뉴를 조회할 수 없습니다.");
        }

        return toMenuResponse(updatedMenu);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long menuId) {
        Store store = getActiveStore(userId);

        int result = menuMapper.softDelete(menuId, store.getId());

        if (result != 1) {
            throw new RuntimeException("삭제할 메뉴를 찾을 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<MenuLossDismissalResponse> findActiveLossDismissals(Long userId) {
        Store store = getActiveStore(userId);

        return menuMapper.findActiveLossDismissalsByStoreId(store.getId())
                .stream()
                .map(this::toMenuLossDismissalResponse)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public MenuLossDismissalResponse dismissLossMenu(Long userId, Long menuId, MenuLossDismissRequest dismissReq) {
        Store store = getActiveStore(userId);
        Menu menu = menuMapper.findByIdAndStoreId(menuId, store.getId());

        if (menu == null) {
            throw new RuntimeException("확인 완료 처리할 메뉴를 찾을 수 없습니다.");
        }

        int hideDays = (dismissReq != null && dismissReq.hideDays() != null) ? dismissReq.hideDays() : 7;
        LocalDateTime hideUntil = LocalDateTime.now().plusDays(hideDays);

        MenuLossDismissal dismissal = MenuLossDismissal.builder()
                .storeId(store.getId())
                .menuId(menuId)
                .hideUntil(hideUntil)
                .build();

        menuMapper.upsertLossDismissal(dismissal);
        MenuLossDismissal savedDismissal = menuMapper.findLossDismissalByStoreIdAndMenuId(store.getId(), menuId);

        if (savedDismissal == null) {
            throw new RuntimeException("숨은 손실 메뉴 확인 완료 저장 후 조회에 실패했습니다.");
        }

        return toMenuLossDismissalResponse(savedDismissal);
    }

    @Transactional(rollbackFor = Exception.class)
    public void restoreLossMenu(Long userId, Long menuId) {
        Store store = getActiveStore(userId);
        Menu menu = menuMapper.findByIdAndStoreId(menuId, store.getId());

        if (menu == null) {
            throw new RuntimeException("다시 표시할 메뉴를 찾을 수 없습니다.");
        }

        menuMapper.restoreLossDismissal(store.getId(), menuId);
    }

    private Store getActiveStore(Long userId) {
        Store store = storeMapper.findByUserId(userId);
        if (store == null) {
            throw new RuntimeException("등록된 활성 매장이 없습니다.");
        }
        return store;
    }

    private MenuResponse toMenuResponse(Menu menu) {
        int expectedMargin = menu.getMenuPrice() - menu.getMenuCost() - menu.getPackagingFee();
        BigDecimal expectedMarginRate;

        if (menu.getMenuPrice() == 0) {
            expectedMarginRate = BigDecimal.ZERO;
        } else {
            expectedMarginRate = BigDecimal.valueOf(expectedMargin)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(menu.getMenuPrice()), 2, RoundingMode.HALF_UP);
        }

        return MenuResponse.builder()
                .id(menu.getId())
                .menuName(menu.getMenuName())
                .menuPrice(menu.getMenuPrice())
                .menuCost(menu.getMenuCost())
                .packagingFee(menu.getPackagingFee())
                .expectedCookingTime(menu.getExpectedCookingTime())
                .batchCapacity(menu.getBatchCapacity())
                .expectedMargin(expectedMargin)
                .expectedMarginRate(expectedMarginRate)
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }

    private MenuLossDismissalResponse toMenuLossDismissalResponse(MenuLossDismissal dismissal) {
        return MenuLossDismissalResponse.builder()
                .id(dismissal.getId())
                .menuId(dismissal.getMenuId())
                .dismissedAt(dismissal.getDismissedAt())
                .hideUntil(dismissal.getHideUntil())
                .build();
    }
}
