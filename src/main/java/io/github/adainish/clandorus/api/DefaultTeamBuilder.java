package io.github.adainish.clandorus.api;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.dialogue.DialogueInputScreen;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.enumeration.DefaultTeamBuilderAction;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.gyms.DefaultTeam;
import io.github.adainish.clandorus.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Arrays;

import static io.github.adainish.clandorus.util.Util.filler;

public class DefaultTeamBuilder {
    private DefaultTeamBuilderAction teamBuilderAction;
    private DefaultTeam defaultTeam;

    public DefaultTeamBuilder() {

    }

    public void save() {
        //save to config and update obj in cache
        Clandorus.defaultTeamRegistry.save(defaultTeam);
    }

    public DefaultTeamBuilder(DefaultTeam defaultTeam) {
        if (defaultTeam == null)
            this.defaultTeam = new DefaultTeam();
    }

    public void openUI(Player player, DefaultTeam defaultTeam)
    {
        if (defaultTeam != null)
            this.defaultTeam = defaultTeam;

        UIManager.openUIForcefully(player.getServerEntity(), MainMenu(player));
    }

    public void closeOrReturn(Player player)
    {
        if (inGymBuilder(player))
            player.getGymBuilder().openEditorUI(player, player.getGymBuilder().getNpcTrainer(), player.getGymBuilder().getGym());
        else UIManager.closeUI(player.getServerEntity());
    }

    public boolean inGymBuilder(Player player)
    {
        return player.getGymBuilder() != null;
    }

    public void updateTeamBuilderCache(Player player) {
        player.setDefaultTeamBuilder(this);
        player.updateCache();
    }

    public DialogueInputScreen.Builder dialogueInputScreenBuilder(DefaultTeamBuilderAction action, Player player) {

        DialogueInputScreen.Builder builder = new DialogueInputScreen.Builder();
        builder.setShouldCloseOnEsc(false);
        switch (action) {
            case specs:
            {
                builder.setTitle(Util.formattedString("&aPokemon Spec"));
                builder.setText(Util.formattedString("&7Provide a spec"));
            }
            case specs_error:
            {
                builder.setTitle(Util.formattedString("&c&lInvalid Spec"));
                builder.setText(Util.formattedString("&cPlease provide a valid Pokemon Spec"));
            }
            case none: {
                break;
            }
        }
        setTeamBuilderAction(action);
        updateTeamBuilderCache(player);
        return builder;
    }

    public GooeyPage MainMenu(Player player)
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(6);

        builder.border(0, 0, 6, 9, filler);

        GooeyButton finish = GooeyButton.builder()
                .title(Util.formattedString("&a&lSave and Finish"))
                .display(new ItemStack(PixelmonItems.up_grade))
                .onClick(b ->
                {
                    save();
                    closeOrReturn(player);
                })
                .build();

        GooeyButton addSpecs = GooeyButton.builder()
                .display(new ItemStack(Items.WRITABLE_BOOK))
                .onClick(b -> {
                    if (defaultTeam.pokemonSpecs.size() >= 6) {
                        Util.send(b.getPlayer(), "&cYou've already reached the max of 6 pokemon specs");
                        return;
                    }
                    dialogueInputScreenBuilder(DefaultTeamBuilderAction.specs, player).sendTo(b.getPlayer());
                })
                .build();

        for (int i = 0; i < 6; i++) {
            GooeyButton button;
            if (i > defaultTeam.pokemonSpecs.size())
            {
                button = GooeyButton.builder()
                        .title(Util.formattedString("&4&lEmpty Slot"))
                        .display(new ItemStack(Items.BARRIER))
                        .build();
            } else {
                String s = defaultTeam.pokemonSpecs.get(i);
                PokemonSpecification specification = PokemonSpecificationProxy.create(s);
                Pokemon p = specification.create();
                button = GooeyButton.builder()
                        .title(Util.formattedString("&e" + p.getSpecies().getName()))
                        .lore(Util.formattedArrayList(Arrays.asList("&7Spec Data:", "&e" + s)))
                        .display(SpriteItemHelper.getPhoto(specification.create()))
                        .build();
            }
            builder.set(0, 3, finish);
            builder.set(0, 5, addSpecs);
            builder.set(1, 1 + (i + 2), button);
        }

        return GooeyPage.builder().template(builder.build()).build();
    }

    public DefaultTeam getDefaultTeam() {
        return defaultTeam;
    }

    public void setDefaultTeam(DefaultTeam defaultTeam) {
        this.defaultTeam = defaultTeam;
    }

    public DefaultTeamBuilderAction getTeamBuilderAction() {
        return teamBuilderAction;
    }

    public void setTeamBuilderAction(DefaultTeamBuilderAction teamBuilderAction) {
        this.teamBuilderAction = teamBuilderAction;
    }
}
