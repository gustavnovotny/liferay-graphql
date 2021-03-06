package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import fr.sedona.liferay.graphql.loaders.AssetEntryBatchLoader;
import fr.sedona.liferay.graphql.resolvers.AssetEntryResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionContextBuilder;
import graphql.execution.ExecutionId;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import org.dataloader.DataLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for {@link AssetEntryResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class AssetEntryResolversImplTest {
    private static final long ENTRY_ID = 987L;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, AssetEntry> dataLoader;

    @InjectMocks
    AssetEntryResolvers resolvers = new AssetEntryResolversImpl();

    @Mock
    private AssetEntryLocalService localService;

    @Before
    public void setUp() {
        executionId = ExecutionId.from("execution-1");
        executionContext = ExecutionContextBuilder.newExecutionContextBuilder()
                .executionId(executionId)
                .build();

        dataLoader = mock(DataLoader.class);
        mockEnvironment = mock(DataFetchingEnvironment.class);
        doReturn(dataLoader)
                .when(mockEnvironment)
                .getDataLoader(AssetEntryBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((AssetEntryResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private DataFetchingEnvironment getTestEnvironment(Map<String, Object> arguments) {
        return new DataFetchingEnvironmentImpl(
                null,
                arguments,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                executionId,
                null,
                null,
                executionContext);
    }

    @Test
    public void getAssetEntriesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetEntry> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetEntry entity = mock(AssetEntry.class);
                    entity.setEntryId(value);
                    availableObjects.add(entity);
                });
        List<AssetEntry> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetEntries(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetEntry> results = resolvers.getAssetEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetEntriesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<AssetEntry> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetEntry entity = mock(AssetEntry.class);
                    entity.setEntryId(value);
                    availableObjects.add(entity);
                });
        List<AssetEntry> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetEntries(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetEntry> results = resolvers.getAssetEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetEntriesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetEntry> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetEntry entity = mock(AssetEntry.class);
                    entity.setEntryId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetEntries(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetEntry> results = resolvers.getAssetEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetEntriesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetEntry> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetEntries(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetEntry> results = resolvers.getAssetEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetEntryDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        AssetEntry expectedResult = mock(AssetEntry.class);
        expectedResult.setEntryId(ENTRY_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("entryId"))
                .thenReturn(ENTRY_ID);
        when(dataLoader.load(ENTRY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<AssetEntry> asyncResult = resolvers.getAssetEntryDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        AssetEntry result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getAssetEntryDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("entryId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<AssetEntry> asyncResult = resolvers.getAssetEntryDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getAssetEntryDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("entryId"))
                .thenReturn(ENTRY_ID);
        when(dataLoader.load(ENTRY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<AssetEntry> asyncResult = resolvers.getAssetEntryDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        AssetEntry result = asyncResult.get();
        assertNull(result);
    }

    // TODO: Implement unit tests for associate/dissociate with AssetTag and AssetCategory
}
