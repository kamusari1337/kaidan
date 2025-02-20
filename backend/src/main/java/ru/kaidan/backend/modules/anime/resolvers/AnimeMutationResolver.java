package ru.kaidan.backend.modules.anime.resolvers;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import ru.kaidan.backend.modules.anime.entities.types.AnimeRaw;
import ru.kaidan.backend.modules.anime.entities.types.ResultMessage;
import ru.kaidan.backend.modules.anime.services.AnimeService;

@Controller
@RequiredArgsConstructor
public class AnimeMutationResolver {

    private final AnimeService animeService;

    @MutationMapping()
    public ResultMessage loadDataSet(@Argument List<AnimeRaw> dataSet) {
        return animeService.addRawAnimeList(dataSet);
    }
}
