package fr.iglee42.yoyos.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import fr.iglee42.yoyos.Yoyos;
import fr.iglee42.yoyos.common.api.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YoyoItem extends TieredItem implements IYoyo {

    private final Float defaultAttackDamage;
    protected final YoyoFactory factory;
    protected RenderOrientation renderOrientation = RenderOrientation.Vertical;
    protected List<EntityInteraction> entityInteractions = new ArrayList<>();
    protected List<BlockInteraction> blockInteractions = new ArrayList<>();

    public YoyoItem(Properties properties, Tier tier, YoyoFactory factory){
        super(tier, properties);
        this.defaultAttackDamage = tier.getAttackDamageBonus() + 3.0f;
        this.factory = factory;
    }

    public YoyoItem(Properties properties, Tier tier){
        this(properties, tier, YoyoEntity::new);
    }
    public YoyoItem(Tier tier){
        this(new Properties().stacksTo(1), tier);
    }

    public YoyoItem addBlockInteraction(BlockInteraction... interaction){
        Collections.addAll(blockInteractions,interaction);
        return this;
    }

    public YoyoItem addEntityInteraction(EntityInteraction... interaction){
        Collections.addAll(entityInteractions,interaction);
        return this;
    }

    public YoyoItem setRenderOrientation(RenderOrientation renderOrientation) {
        this.renderOrientation = renderOrientation;
        return this;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level p_41422_, List<Component> tooltips, TooltipFlag p_41424_) {
        super.appendHoverText(stack, p_41422_, tooltips, p_41424_);
        tooltips.add(Component.translatable("tooltip.yoyos.weight", getWeight(stack)));
        tooltips.add(Component.translatable("tooltip.yoyos.length", getLength(stack)));

        int duration = getDuration(stack);
        if (duration < 0) tooltips.add(Component.translatable("tooltip.yoyos.duration.infinite"));
        else tooltips.add(Component.translatable("tooltip.yoyos.duration", duration / 20f));

        if (stack.isEnchanted())
            tooltips.add(Component.literal(""));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment == Enchantments.SWEEPING_EDGE) return false;
        return (interactsWithBlocks(stack) && enchantment == Enchantments.BLOCK_FORTUNE) || enchantment.category == EnchantmentCategory.BREAKABLE || enchantment.category == EnchantmentCategory.WEAPON;
    }

    @Override
    public boolean isFoil(ItemStack p_41453_) {
        return super.isFoil(p_41453_);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && state.getDestroySpeed(level,pos) != 0.0F){
            stack.hurtAndBreak(1,entity,e->e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide){
            if (stack.getDamageValue() <= stack.getMaxDamage()){

                YoyoEntity yoyoEntity = YoyoEntity.CASTERS.get(player.getUUID());

                if (yoyoEntity == null) {
                    yoyoEntity = factory.create(level,player, hand);
                    level.addFreshEntity(yoyoEntity);
                    //level.playSound(null, it.posX, it.posY, it.posZ, ModSounds.yoyoThrow, SoundCategory.NEUTRAL, 0.5f, 0.4f / (Item.random.nextFloat() * 0.4f + 0.8f))


                    player.causeFoodExhaustion(0.05f);
                } else {
                    yoyoEntity.setRetracting(!yoyoEntity.isRetracting());
                }

            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1,attacker,e->e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute,AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getAttackDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4000000953674316, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
    }

    public double getAttackDamage(ItemStack yoyo) {
        return defaultAttackDamage.doubleValue();
    }


    @Override
    public double getWeight(ItemStack yoyo) {
        return 1.7;
    }

    @Override
    public double getLength(ItemStack yoyo) {
        return 9.0;
    }

    @Override
    public int getDuration(ItemStack yoyo) {
        return 400;
    }

    @Override
    public int getAttackInterval(ItemStack yoyo) {
        return 10;
    }

    @Override
    public int getMaxCollectedDrops(ItemStack yoyo) {
        return calculateMaxCollectedDrops(0);
    }


    @Override
    public <T extends LivingEntity> void damageItem(ItemStack yoyo, InteractionHand hand, int amount, T entity) {
        yoyo.hurtAndBreak(amount,entity,e->e.broadcastBreakEvent(hand));
    }

    @Override
    public void entityInteraction(ItemStack yoyoStack, Player player, InteractionHand hand, YoyoEntity yoyo, Entity target) {
        if (target.level().isClientSide) return;
        entityInteractions.forEach(i->i.apply(yoyoStack, player, hand, yoyo, target));

    }

    @Override
    public boolean interactsWithBlocks(ItemStack yoyo) {
        return !blockInteractions.isEmpty();
    }

    @Override
    public void blockInteraction(ItemStack yoyoStack, Player player, Level world, BlockPos pos, BlockState state, Block block, YoyoEntity yoyo) {
        if (world.isClientSide) return;
        blockInteractions.forEach(i->i.apply(yoyoStack, player, pos, state, block, yoyo));
    }

    @Override
    public RenderOrientation getRenderOrientation(ItemStack yoyo) {
        return renderOrientation;
    }

    private int calculateMaxCollectedDrops(int level) {
        if (level == 0) return 0;

        var mult = 1;

        for (int i = 0; i < level; i++) mult *= 2;

        return 64 /*YoyosConfig.general.collectingBase.get()*/ * mult;
    }
}
