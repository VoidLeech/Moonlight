package net.mehvahdjukaar.moonlight.resources.recipe;

import com.google.gson.JsonObject;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TemplateRecipeManager {

    private static final Map<ResourceLocation, Function<JsonObject, ? extends IRecipeTemplate<?>>> DESERIALIZERS = new HashMap<>();

    /**
     * Registers a recipe template deserializer. Will be used to parse existing recipes and be able to merge new ones
     *
     * @param deserializer usually IRecipeTemplate::new
     * @param serializer   recipe serializer type
     */
    public static <T extends IRecipeTemplate<?>> void registerTemplate(
            RecipeSerializer<?> serializer, Function<JsonObject, T> deserializer) {
        registerTemplate(Utils.getID(serializer), deserializer);
    }

    public static <T extends IRecipeTemplate<?>> void registerTemplate(
            ResourceLocation serializerId, Function<JsonObject, T> deserializer) {
        DESERIALIZERS.put(serializerId, deserializer);
    }

    public static IRecipeTemplate<?> read(JsonObject recipe) throws UnsupportedOperationException {
        String type = GsonHelper.getAsString(recipe, "type");
        //RecipeSerializer<?> s = ForgeRegistries.RECIPE_SERIALIZERS.getValue(new ResourceLocation(type));

        var templateFactory = DESERIALIZERS.get(new ResourceLocation(type));

        if (templateFactory != null) {
            var template = templateFactory.apply(recipe);
            //special case for shaped with a single item...
            if (template instanceof ShapedRecipeTemplate st && st.shouldBeShapeless()) {
                template = st.toShapeless();
            }
            addRecipeConditions(recipe, template);
            return template;
        } else {
            throw new UnsupportedOperationException(String.format("Invalid recipe serializer: %s. Must be either shaped, shapeless or stonecutting", type));
        }
    }

    @ExpectPlatform
    private static void addRecipeConditions(JsonObject recipe, IRecipeTemplate<?> template) {
        throw new AssertionError();
    }


    static {
        registerTemplate(RecipeSerializer.SHAPED_RECIPE, ShapedRecipeTemplate::new);
        registerTemplate(RecipeSerializer.SHAPELESS_RECIPE, ShapelessRecipeTemplate::new);
        registerTemplate(RecipeSerializer.STONECUTTER, StoneCutterRecipeTemplate::new);
    }

}
