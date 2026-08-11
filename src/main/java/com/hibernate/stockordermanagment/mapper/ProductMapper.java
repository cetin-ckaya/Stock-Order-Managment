package com.hibernate.stockordermanagment.mapper;

import com.hibernate.stockordermanagment.dto.request.CreateProductRequest;
import com.hibernate.stockordermanagment.dto.request.UpdateProductRequest;
import com.hibernate.stockordermanagment.dto.response.ProductResponse;
import com.hibernate.stockordermanagment.entity.Product;
import org.mapstruct.*;

/**
 * Product Mapper
 *
 * TASK GEREKSİNİMİ: Entity <-> DTO dönüşümlerini yönetir.
 *
 * NEDEN MAPSTRUCT?
 * - Manuel dönüşüm yazmak yerine otomatik kod üretir
 * - Derleme zamanında (compile-time) çalışır, hataları erken yakalar
 * - Performanslı: Reflection kullanmaz, direkt Java kodu üretir
 *
 * componentModel = "spring":
 * - Bu mapper'ı Spring Bean olarak tanımlar
 * - @Autowired ile inject edilebilir hale getirir
 *
 * unmappedTargetPolicy = ReportingPolicy.IGNORE:
 * - Hedef sınıfta eşleşmeyen alan varsa hata vermez
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {

    /**
     * CreateRequest -> Entity dönüşümü
     * Yeni ürün oluştururken kullanılır
     *
     * NOT: id, createdAt, updatedAt, version alanları
     * JPA tarafından otomatik set edilir, burada ignore edilir
     */
    Product toEntity(CreateProductRequest request);

    /**
     * Entity -> Response dönüşümü
     * API'den veri dönerken kullanılır
     *
     * @Mapping: isLowStock alanı entity'de bir metod,
     * MapStruct bunu otomatik eşleştiremez.
     * Bu yüzden expression ile manuel hesaplıyoruz.
     *
     * TASK GEREKSİNİMİ: "Stok seviyesi düşük ürünlerin listelenmesi"
     */
    @Mapping(target = "isLowStock", expression = "java(product.isLowStock())")
    ProductResponse toResponse(Product product);

    /**
     * UpdateRequest -> Entity güncellemesi
     * Var olan ürünü güncellerken kullanılır
     *
     * @BeanMapping + NullValuePropertyMappingStrategy.IGNORE:
     * - Sadece null olmayan alanları günceller
     * - Örnek: Sadece price gönderdiysen sadece price güncellenir
     * - Buna "Partial Update" (Kısmi Güncelleme) denir
     *
     * @MappingTarget: Hedef nesnenin var olan entity olduğunu belirtir
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateProductRequest request, @MappingTarget Product product);
}